package com.ksptool.bio.biz.aacp.service;

import com.ksptool.bio.biz.aacp.commons.McpClientSession;
import com.ksptool.bio.biz.aacp.commons.McpParser;
import com.ksptool.bio.biz.aacp.commons.jrpc.InputMethods;
import com.ksptool.bio.biz.aacp.commons.jrpc.RpcInput;
import com.ksptool.bio.biz.aacp.commons.jrpc.RpcOutput;
import com.ksptool.bio.biz.aacp.commons.jrpc.dto.InitializeDto;
import com.ksptool.bio.biz.aacp.model.AacpCapabilityPo;
import com.ksptool.bio.biz.aacp.repository.AacpCapabilityRepository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * MCP 业务逻辑层
 */
@Slf4j
@Service
public class AacpEndpointService {

    @Autowired
    private AacpCapabilityRepository aacpCapabilityRepository;

    /**
     * 处理入向 JSON-RPC 请求
     */
    public RpcOutput<?> inbound(McpClientSession session, RpcInput<String> input) {

        var p = McpParser.of(input);

        //---- 客户端握手 ----
        if (p.getMethod() == InputMethods.INITIALIZE) {
            p.as(InitializeDto.class);
            log.info("[AACP] 初始化 Inbound => {}", session.getSessionId());
        }

        //---- 客户端就绪 ----
        if (p.getMethod() == InputMethods.INITIALIZED_NOTIFICATION) {
            session.setStatus(1);
            log.info("[AACP] 客户端已就绪 Inbound => {}", session.getSessionId());
        }

        //---- 请求工具列表 ----
        if (p.getMethod() == InputMethods.TOOLS_LIST) {
            log.info("[AACP] 客户端请求工具列表 Inbound => {}", session.getSessionId());

            List<AacpCapabilityPo> capabilities = aacpCapabilityRepository.getByMcpId(session.getServerId());

            log.info("[AACP] 获取到能力包数量: {}", capabilities.size());
        }

        return null;
    }
}
