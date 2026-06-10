package com.ksptool.bio.biz.aacp.controller;

import com.google.gson.Gson;
import com.ksptool.assembly.entity.exception.BizException;
import com.ksptool.bio.biz.aacp.commons.McpClientSession;
import com.ksptool.bio.biz.aacp.commons.McpParser;
import com.ksptool.bio.biz.aacp.model.AacpMcpPo;
import com.ksptool.bio.biz.aacp.repository.AacpMcpRepository;
import com.ksptool.bio.biz.aacp.service.AacpEndpointService;
import com.ksptool.bio.biz.auth.common.DynamicGlobalWhiteManager;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.http.MediaType;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * MCP 协议端点：SSE 传输层 + JSON-RPC 收发包
 */
@Slf4j
@RestController
@RequestMapping("/aacp")
@Tag(name = "MCP服务器", description = "MCP服务器")
public class AacpEndpoint {

    private final Gson gson = new Gson();
    private final Map<String, McpClientSession> sessionMap = new ConcurrentHashMap<>();

    @Autowired
    private AacpEndpointService aacpEndpointService;

    @Autowired
    private AacpMcpRepository aacpMcpRepository;

    @Autowired
    private DynamicGlobalWhiteManager dgwm;

    @EventListener(ApplicationReadyEvent.class)
    public void init() {
        dgwm.getWhiteListMatchers().add(PathPatternRequestMatcher.pathPattern("/aacp/upstream/**"));
        dgwm.getWhiteListMatchers().add(PathPatternRequestMatcher.pathPattern("/aacp/inbound"));
    }

    /**
     * 建立 SSE 连接
     */
    @GetMapping(value = "/upstream/{code}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter upstream(@PathVariable("code") String code) throws BizException {

        AacpMcpPo po = aacpMcpRepository.getByCode(code);
        if (po == null) {
            throw new BizException("MCP服务器不存在:" + code);
        }
        if (po.getStatus() != 1) {
            throw new BizException("MCP服务器当前不接受连接请求:" + code);
        }

        String sessionId = UUID.randomUUID().toString();
        SseEmitter emitter = new SseEmitter(3600000L);

        McpClientSession session = new McpClientSession(sessionId, code, po.getId(), emitter);
        sessionMap.put(sessionId, session);
        emitter.onCompletion(() -> sessionMap.remove(sessionId));
        emitter.onTimeout(() -> sessionMap.remove(sessionId));

        try {
            log.info("[AACP] 创建会话 Upstream => {} 服务器编码:{}", sessionId, code);
            emitter.send(SseEmitter.event()
                    .name("endpoint")
                    .data("/aacp/inbound?sessionId=" + sessionId));
        } catch (IOException e) {
            sessionMap.remove(sessionId);
            log.error("[AACP] 创建会话 Upstream => {} 服务器编码:{} 异常:{}", sessionId, code, e.getMessage());
        }
        return emitter;
    }

    /**
     * 处理 JSON-RPC 请求
     */
    @PostMapping(value = "/inbound", consumes = "application/json")
    public void inbound(@RequestParam("sessionId") String sessionId, @RequestBody String rawBody) {

        McpClientSession session = sessionMap.get(sessionId);
        if (session == null) {
            throw new RuntimeException("Session not found");
        }

        var p = McpParser.of(rawBody);
        var result = aacpEndpointService.inbound(session, p.getInput());

        try {
            session.getEmitter().send(SseEmitter.event().data(gson.toJson(result)));
        } catch (IOException e) {
        }
    }
}
