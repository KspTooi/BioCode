package com.ksptool.bio.biz.aacp.controller;

import com.google.gson.Gson;
import com.ksptool.assembly.entity.exception.BizException;
import com.ksptool.bio.biz.aacp.commons.McpClientSession;
import com.ksptool.bio.biz.aacp.commons.McpParser;
import com.ksptool.bio.biz.aacp.service.AacpAccessService;
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


@Slf4j
@RestController
@RequestMapping("/aacp")
@Tag(name = "AACP访问端点", description = "该端点为AACP协议的访问端点，用于接收J-RPC协议的请求")
public class AacpAccessController {

    private final Gson gson = new Gson();

    @Autowired
    private AacpAccessService aacpAccessService;

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
        //SSE 长连接不得持有 JDBC 连接,读库已下沉到 Service 的工作线程执行,请求线程全程不绑定 EntityManager/连接,规避 OSIV 占用 HikariCP
        SseEmitter emitter = new SseEmitter(3600000L);
        aacpAccessService.createSession(code, emitter);
        return emitter;
    }

    /**
     * 处理 JSON-RPC 请求
     */
    @PostMapping(value = "/inbound", consumes = "application/json")
    public void inbound(@RequestParam("sessionId") String sessionId, @RequestBody String rawBody) {

        McpClientSession session = aacpAccessService.getSession(sessionId);
        if (session == null) {
            throw new RuntimeException("Session not found");
        }

        var p = McpParser.of(rawBody);
        var result = aacpAccessService.inbound(session, p.getInput());

        try {
            session.getEmitter().send(SseEmitter.event().data(gson.toJson(result)));
        } catch (IOException e) {
        }
    }
}
