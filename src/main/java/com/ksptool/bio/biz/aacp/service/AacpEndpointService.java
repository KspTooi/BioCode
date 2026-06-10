package com.ksptool.bio.biz.aacp.service;

import com.google.gson.Gson;
import com.ksptool.assembly.entity.exception.BizException;
import com.ksptool.bio.biz.aacp.commons.McpParser;
import com.ksptool.bio.biz.aacp.commons.jrpc.InputMethods;
import com.ksptool.bio.biz.aacp.commons.jrpc.RpcInput;
import com.ksptool.bio.biz.aacp.commons.jrpc.RpcOutput;
import com.ksptool.bio.biz.aacp.commons.jrpc.dto.InitializeDto;
import com.ksptool.bio.biz.aacp.model.AacpMcpPo;
import com.ksptool.bio.biz.aacp.repository.AacpMcpRepository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * MCP  业务逻辑层
 */
@Slf4j
@Service
public class AacpEndpointService {

    private final Gson g = new Gson();

    @Autowired
    private AacpMcpRepository aacpMcpRepository;

    /**
     * 校验 MCP 编码是否合法且可接受连接
     */
    public void validateCode(String code) throws BizException {
        AacpMcpPo po = aacpMcpRepository.findByCode(code);
        if (po == null) {
            throw new BizException("MCP服务器不存在:" + code);
        }
        if (po.getStatus() != 1) {
            throw new BizException("MCP服务器当前不接受连接请求:" + code);
        }
    }

   /**
    * 处理入向 JSON-RPC 请求
    */
    public RpcOutput<?> inbound(RpcInput<String> input) {

        var p = McpParser.of(input);

        //---- 生命周期 ----
        if (p.getMethod() == InputMethods.INITIALIZE) {

            var initializeDto = p.as(InitializeDto.class);

            log.info("MCP客户端握手: {}", g.toJson(input));

        }

        throw new RuntimeException("Method not found: " + p.getMethod().getKey());
    }
}
