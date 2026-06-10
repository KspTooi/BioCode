package com.ksptool.bio.biz.aacp.controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.ksptool.assembly.entity.exception.BizException;
import com.ksptool.bio.biz.aacp.commons.McpParser;
import com.ksptool.bio.biz.aacp.commons.jrpc.RpcInput;
import com.ksptool.bio.biz.aacp.service.AacpEndpointService;
import com.ksptool.bio.biz.auth.common.DynamicGlobalWhiteManager;
import com.ksptool.bio.commons.annotation.PrintLog;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@PrintLog
@RestController
@RequestMapping("/aacp")
@Tag(name = "MCP服务器", description = "MCP服务器")
public class AacpEndpoint {

    private final Gson gson = new Gson();
    private final Map<String, SseEmitter> sessionMap = new ConcurrentHashMap<>();

    @Autowired
    private AacpEndpointService aacpEndpointService;

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

        aacpEndpointService.validateCode(code);

        String sessionId = UUID.randomUUID().toString();
        SseEmitter emitter = new SseEmitter(3600000L);

        sessionMap.put(sessionId, emitter);
        emitter.onCompletion(() -> sessionMap.remove(sessionId));
        emitter.onTimeout(() -> sessionMap.remove(sessionId));

        try {
            emitter.send(SseEmitter.event()
                    .name("endpoint")
                    .data("/aacp/inbound?sessionId=" + sessionId));
        } catch (IOException e) {
            sessionMap.remove(sessionId);
        }
        return emitter;
    }

    /**
     * 处理 JSON-RPC 请求
     */
    @PostMapping(value = "/inbound", consumes = "application/json")
    public void inbound(@RequestParam("sessionId") String sessionId, @RequestBody String rawBody) {

        SseEmitter emitter = sessionMap.get(sessionId);
        if (emitter == null) {
            throw new RuntimeException("Session not found");
        }

        var p = McpParser.of(rawBody);
        var result = aacpEndpointService.inbound(p.getInput());
        
        try {
            emitter.send(SseEmitter.event().data(gson.toJson(result)));
        } catch (IOException e) {
        }
    }
}
