package com.ksptool.bio.biz.aacp.service;

import com.ksptool.bio.biz.aacp.commons.McpClientSession;
import com.ksptool.bio.biz.aacp.commons.McpParser;
import com.ksptool.bio.biz.aacp.commons.MicroFuncDefinition;
import com.ksptool.bio.biz.aacp.commons.jrpc.InputMethods;
import com.ksptool.bio.biz.aacp.commons.jrpc.RpcInput;
import com.ksptool.bio.biz.aacp.commons.jrpc.RpcOutput;
import com.ksptool.bio.biz.aacp.commons.jrpc.dto.ToolsCallDto;
import com.ksptool.bio.biz.aacp.commons.jrpc.vo.InitializeVo;
import com.ksptool.bio.biz.aacp.commons.jrpc.vo.PingVo;
import com.ksptool.bio.biz.aacp.commons.jrpc.vo.ToolsCallVo;
import com.ksptool.bio.biz.aacp.commons.jrpc.vo.ToolsListVo;
import com.ksptool.bio.biz.aacp.model.AacpCapabilityPo;
import com.ksptool.bio.biz.aacp.model.AacpFuncPo;
import com.ksptool.bio.biz.aacp.repository.AacpCapabilityFuncRepository;
import com.ksptool.bio.biz.aacp.repository.AacpCapabilityRepository;
import com.ksptool.bio.biz.aacp.repository.AacpFuncRepository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import com.ksptool.bio.biz.aacp.commons.MicroFuncRegistry;

/**
 * MCP 协议业务逻辑：JSON-RPC 方法路由分发
 * <p>
 * 统一接入点，根据 InputMethods 枚举路由到对应处理分支。
 * 工具列表与调用委托给 MicroFuncService。
 */
@Slf4j
@Service
public class AacpEndpointService {

    @Autowired
    private MicroFuncService microFuncService;

    @Autowired
    private AacpCapabilityRepository aacpCapabilityRepository;

    @Autowired
    private AacpCapabilityFuncRepository capabilityFuncRepository;

    @Autowired
    private AacpFuncRepository aacpFuncRepository;

    @Autowired
    private MicroFuncRegistry mfRegistry;

    /**
     * 处理入向 JSON-RPC 请求，按方法名路由分发
     *
     * @param session 客户端会话
     * @param input   原始 JSON-RPC 请求
     * @return JSON-RPC 响应，通知类方法返回 null
     */
    public RpcOutput<?> inbound(McpClientSession session, RpcInput<String> input) {

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
            var funcCapPos = aacpCapabilityRepository.getByMcpId(session.getServerId(),0);
            var funcCapIds = funcCapPos.stream().map(AacpCapabilityPo::getId).collect(Collectors.toSet());

            //获取能力包中的微函数
            var funcPos = aacpFuncRepository.findAllById(funcCapIds);

            //获取已注册的微函数Bean列表
            var mfBeanNames = mfRegistry.getAll().stream().map(MicroFuncDefinition::getBean).collect(Collectors.toSet());

            //直接组装为工具列表
            var tools = new ArrayList<ToolsListVo.Tool>();
            

            for(var fPo : funcPos){

                //如果数据库中的微函数对应的Bean不存在 则跳过
                if(!mfBeanNames.contains(fPo.getTarget())){
                    continue;
                }

                var t = new ToolsListVo.Tool();
                t.setName(fPo.getCode());
                t.setDescription(fPo.getDescription());
                t.setInputSchema(new LinkedHashMap<>());
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
            ToolsCallVo vo = microFuncService.call(callDto.getName(), callDto.getArguments());
            return RpcOutput.success(input.getId(), vo);
        }

        return RpcOutput.error(input.getId(), -32601, "Method not found: " + input.getMethod());
    }


}
