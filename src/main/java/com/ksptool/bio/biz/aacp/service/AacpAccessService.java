package com.ksptool.bio.biz.aacp.service;

import com.ksptool.assembly.entity.exception.BizException;
import com.ksptool.bio.biz.aacp.commons.McpClientSession;
import com.ksptool.bio.biz.aacp.commons.McpParser;
import com.ksptool.bio.biz.aacp.commons.MicroFuncDefinition;
import com.ksptool.bio.biz.aacp.commons.MicroFuncRegistry;
import com.ksptool.bio.biz.aacp.commons.jrpc.InputMethods;
import com.ksptool.bio.biz.aacp.commons.jrpc.RpcInput;
import com.ksptool.bio.biz.aacp.commons.jrpc.RpcOutput;
import com.ksptool.bio.biz.aacp.commons.jrpc.dto.ToolsCallDto;
import com.ksptool.bio.biz.aacp.commons.jrpc.vo.InitializeVo;
import com.ksptool.bio.biz.aacp.commons.jrpc.vo.PingVo;
import com.ksptool.bio.biz.aacp.commons.jrpc.vo.ToolsCallVo;
import com.ksptool.bio.biz.aacp.commons.jrpc.vo.ToolsListVo;
import com.ksptool.bio.biz.aacp.model.agenthub.AacpAgentHubPo;
import com.ksptool.bio.biz.aacp.model.cap.AacpCapPo;
import com.ksptool.bio.biz.aacp.repository.AgentHubRepository;
import com.ksptool.bio.biz.aacp.repository.CapRepository;
import com.ksptool.bio.biz.aacp.repository.MicroFuncRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;


@Slf4j
@Service
public class AacpAccessService {

    private final Map<String, McpClientSession> sessionMap = new ConcurrentHashMap<>();

    @Autowired
    private MicroFuncCallService microFuncCallService;

    @Autowired
    private CapRepository capRepository;

    @Autowired
    private MicroFuncRepository microFuncRepository;

    @Autowired
    private MicroFuncRegistry mfRegistry;

    @Autowired
    private AgentHubRepository agentHubRepository;

    /**
     * 创建 SSE 会话
     *
     * @param serverCode 智能体枢纽编码
     * @param emitter    SSE 发射器
     * @throws BizException Hub 不存在或不可用
     */
    public String createSession(String serverCode, SseEmitter emitter) throws BizException {

        //SSE 是异步长连接,且本项目强制开启 OSIV,若直接在请求线程查库,EntityManager 与其 JDBC 连接会被 OSIV 绑定到整个 SSE 生命周期(可达1小时)而无法归还 HikariCP
        //把读库丢到无 OSIV 绑定的工作线程,该线程查询用完即关闭 EntityManager 并归还连接,SSE 请求线程全程不碰库
        AacpAgentHubPo po;
        try {
            po = CompletableFuture.supplyAsync(() -> agentHubRepository.getByCode(serverCode)).join();
        } catch (CompletionException e) {
            throw new BizException("查询智能体枢纽失败:" + serverCode);
        }

        if (po == null) {
            throw new BizException("智能体枢纽不存在:" + serverCode);
        }
        if (po.getStatus() != 1) {
            throw new BizException("智能体枢纽当前不接受连接请求:" + serverCode);
        }


        String sessionId = UUID.randomUUID().toString();
        McpClientSession session = new McpClientSession(sessionId, serverCode, po.getId(), po.getName(), emitter);
        sessionMap.put(sessionId, session);
        emitter.onCompletion(() -> closeSession(sessionId));
        emitter.onTimeout(() -> closeSession(sessionId));

        log.info("[AACP] 创建会话 Upstream => {} 服务器编码:{}", sessionId, serverCode);

        try {
            emitter.send(SseEmitter.event()
                    .name("endpoint")
                    .data("/aacp/inbound?sessionId=" + sessionId));
        } catch (IOException e) {
            closeSession(sessionId);
            log.error("[AACP] 创建会话 Upstream => {} 服务器编码:{} 异常:{}", sessionId, serverCode, e.getMessage());
        }

        return sessionId;
    }

    /**
     * 根据 sessionId 获取会话
     *
     * @param sessionId 会话ID
     * @return 会话，不存在返回 null
     */
    public McpClientSession getSession(String sessionId) {
        return sessionMap.get(sessionId);
    }

    /**
     * 关闭会话并从内存中移除
     *
     * @param sessionId 会话ID
     */
    public void closeSession(String sessionId) {
        McpClientSession session = sessionMap.remove(sessionId);
        if (session == null) {
            return;
        }
        session.getEmitter().complete();
    }

    /**
     * 获取所有在线会话列表
     *
     * @return 在线会话列表
     */
    public List<McpClientSession> getOnlineSessionList() {
        return new ArrayList<>(sessionMap.values());
    }

    /**
     * 处理入向 JSON-RPC 请求，按方法名路由分发
     *
     * @param session 客户端会话
     * @param input   原始 JSON-RPC 请求
     * @return JSON-RPC 响应，通知类方法返回 null
     */
    @Transactional
    public RpcOutput<?> inbound(McpClientSession session, RpcInput<String> input) {

        session.setInboundCount(session.getInboundCount() + 1);

        var p = McpParser.of(input);

        //客户端握手
        if (p.getMethod() == InputMethods.INITIALIZE) {
            log.info("[AACP] 初始化 Inbound => {}", session.getSessionId());

            var ret = new InitializeVo();
            ret.setProtocolVersion("2025-11-25");

            var caps = new InitializeVo.ServerCapabilities();
            var toolsCaps = new InitializeVo.CapabilityListChanged();
            toolsCaps.setListChanged(true);
            caps.setTools(toolsCaps);

            var serverInfo = new InitializeVo.ServerInfo();
            serverInfo.setName("AACP-VIA-BIO-SERVER");
            serverInfo.setVersion("1.7B(1)");
            ret.setServerInfo(serverInfo);
            ret.setCapabilities(caps);
            return RpcOutput.success(input.getId(), ret);
        }

        //客户端就绪
        if (p.getMethod() == InputMethods.INITIALIZED_NOTIFICATION) {
            session.setStatus(1);
            log.info("[AACP] 客户端已就绪 Inbound => {}", session.getSessionId());
            return null;
        }

        //心跳检测
        if (p.getMethod() == InputMethods.PING) {
            PingVo vo = new PingVo();
            return RpcOutput.success(input.getId(), vo);
        }

        //工具列表
        if (p.getMethod() == InputMethods.TOOLS_LIST) {
            log.info("[AACP] 客户端请求工具列表 Inbound => {}", session.getSessionId());

            var ret = new ToolsListVo();

            //获取微函数能力包
            var funcCapPos = capRepository.getByHubId(session.getServerId(), 0);
            var funcCapIds = funcCapPos.stream().map(AacpCapPo::getId).collect(Collectors.toSet());

            //获取能力包中的微函数
            var funcPos = microFuncRepository.getMicroFuncListByCapIds(funcCapIds);

            //直接组装为工具列表
            var tools = new ArrayList<ToolsListVo.Tool>();


            for (var fPo : funcPos) {

                //查找已注册的微函数
                MicroFuncDefinition def = mfRegistry.get(fPo.getTarget());

                if (def == null) {
                    continue;
                }

                var t = new ToolsListVo.Tool();
                t.setName(fPo.getCode());
                t.setDescription(fPo.getDescription());
                t.setInputSchema(def.getInputSchema());
                tools.add(t);
            }

            ret.setTools(tools);
            return RpcOutput.success(input.getId(), ret);
        }

        //---- 工具调用 ----
        if (p.getMethod() == InputMethods.TOOLS_CALL) {
            ToolsCallDto callDto = p.as(ToolsCallDto.class);
            if (callDto == null) {
                return RpcOutput.error(input.getId(), -32602, "Invalid params");
            }
            log.info("[AACP] 客户端调用工具: name={} Inbound => {}", callDto.getName(), session.getSessionId());
            ToolsCallVo vo = microFuncCallService.call(callDto.getName(), callDto.getArguments());
            return RpcOutput.success(input.getId(), vo);
        }

        return RpcOutput.error(input.getId(), -32601, "Method not found: " + input.getMethod());
    }


}
