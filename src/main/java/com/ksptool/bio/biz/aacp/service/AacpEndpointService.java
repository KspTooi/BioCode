package com.ksptool.bio.biz.aacp.service;

import com.ksptool.bio.biz.aacp.commons.McpClientSession;
import com.ksptool.bio.biz.aacp.commons.McpParser;
import com.ksptool.bio.biz.aacp.commons.jrpc.InputMethods;
import com.ksptool.bio.biz.aacp.commons.jrpc.RpcInput;
import com.ksptool.bio.biz.aacp.commons.jrpc.RpcOutput;
import com.ksptool.bio.biz.aacp.commons.jrpc.dto.ToolsCallDto;
import com.ksptool.bio.biz.aacp.commons.jrpc.vo.InitializeVo;
import com.ksptool.bio.biz.aacp.commons.jrpc.vo.PingVo;
import com.ksptool.bio.biz.aacp.commons.jrpc.vo.ToolsCallVo;
import com.ksptool.bio.biz.aacp.commons.jrpc.vo.ToolsListVo;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

    /**
     * 处理入向 JSON-RPC 请求，按方法名路由分发
     *
     * @param session 客户端会话
     * @param input   原始 JSON-RPC 请求
     * @return JSON-RPC 响应，通知类方法返回 null
     */
    public RpcOutput<?> inbound(McpClientSession session, RpcInput<String> input) {

        var p = McpParser.of(input);

        //---- 客户端握手 ----
        if (p.getMethod() == InputMethods.INITIALIZE) {
            log.info("[AACP] 初始化 Inbound => {}", session.getSessionId());
            InitializeVo vo = buildInitializeVo();
            return RpcOutput.success(input.getId(), vo);
        }

        //---- 客户端就绪（通知，无需响应） ----
        if (p.getMethod() == InputMethods.INITIALIZED_NOTIFICATION) {
            session.setStatus(1);
            log.info("[AACP] 客户端已就绪 Inbound => {}", session.getSessionId());
            return null;
        }

        //---- 心跳 ----
        if (p.getMethod() == InputMethods.PING) {
            PingVo vo = new PingVo();
            return RpcOutput.success(input.getId(), vo);
        }

        //---- 工具列表 ----
        if (p.getMethod() == InputMethods.TOOLS_LIST) {
            log.info("[AACP] 客户端请求工具列表 Inbound => {}", session.getSessionId());
            ToolsListVo vo = microFuncService.buildToolsList();
            return RpcOutput.success(input.getId(), vo);
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

    /**
     * 构建 initialize 握手响应 Vo（屏蔽 InitializeVo 内嵌结构构造复杂度）
     */
    private InitializeVo buildInitializeVo() {
        InitializeVo vo = new InitializeVo();
        vo.setProtocolVersion("2024-11-05");

        InitializeVo.ServerCapabilities caps = new InitializeVo.ServerCapabilities();
        InitializeVo.CapabilityListChanged toolsCaps = new InitializeVo.CapabilityListChanged();
        toolsCaps.setListChanged(true);
        caps.setTools(toolsCaps);

        InitializeVo.ServerInfo serverInfo = new InitializeVo.ServerInfo();
        serverInfo.setName("AACP-Server");
        serverInfo.setVersion("1.0.0");
        vo.setServerInfo(serverInfo);

        return vo;
    }
}
