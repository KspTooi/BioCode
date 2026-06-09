package com.ksptool.bio.biz.aacp.controller;

import com.google.gson.Gson;
import com.ksptool.bio.biz.aacp.commons.McpRpcDto;
import com.ksptool.bio.biz.aacp.commons.McpRpcResult;
import com.ksptool.bio.biz.aacp.service.AacpMcpService;
import com.ksptool.bio.biz.auth.common.DynamicGlobalWhiteManager;
import com.ksptool.bio.commons.annotation.PrintLog;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.http.MediaType;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * MCP 协议端点：提供 SSE 传输层与 JSON-RPC 2.0 协议处理
 * <p>
 * 客户端先通过 GET /aacpMcp/upstream/{code} 建立 SSE 连接，
 * 然后往 POST /aacpMcp/request 发送 JSON-RPC 请求。
 */
@PrintLog
@RestController
@RequestMapping("/aacp")
@Tag(name = "MCP服务器", description = "MCP服务器")
@Slf4j
public class AacpEndpoint {

    //存放 SessionID 对应的 SSE 连接
    private final Map<String, SseEmitter> sessionMap = new ConcurrentHashMap<>();

    //JSON序列化器
    private final Gson gson = new Gson();

    @Autowired
    private AacpMcpService aacpMcpService;

    @Autowired
    private DynamicGlobalWhiteManager dgwm;

    /**
     * 应用就绪后注册 MCP 端点白名单，允许未登录客户端访问
     */
    @EventListener(ApplicationReadyEvent.class)
    public void init() {
        log.info("AACP-VIA-BIO 正在配置MCP服务器端点白名单。");
        dgwm.getWhiteListMatchers().add(PathPatternRequestMatcher.pathPattern("/aacpMcp/upstream/**"));
        dgwm.getWhiteListMatchers().add(PathPatternRequestMatcher.pathPattern("/aacpMcp/request"));
    }

    /**
     * 客户端首先请求这里建立 SSE 连接
     */
    @GetMapping(value = "/upstream/{code}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter upstream(@PathVariable("code") String code, @RequestHeader(value = "Authorization", required = false) String authHeader) {

        log.info("AACP-VIA-BIO 客户端建立SSE连接，虚拟MCP编码: {}", code);

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
     * 客户端将所有的 JSON-RPC 请求 POST 到这里
     */
    @PostMapping(value = "/inbound", consumes = "application/json")
    public void request(@RequestParam("sessionId") String sessionId, @RequestBody @Valid McpRpcDto jsonRpcMessage) {

        SseEmitter emitter = sessionMap.get(sessionId);
        if (emitter == null) {
            throw new RuntimeException("Session not found");
        }

        String method = jsonRpcMessage.getMethod();
        if (StringUtils.isEmpty(method)) {
            return;
        }

        McpRpcResult<?> response = null;

        if ("initialize".equals(method)) {
            Map<String, Object> serverInfo = new LinkedHashMap<>();
            serverInfo.put("name", "bio-code-aacp");
            serverInfo.put("version", "1.0.0");

            Map<String, Object> result = new LinkedHashMap<>();
            result.put("protocolVersion", "2025-11-25");
            result.put("capabilities", new LinkedHashMap<>());
            result.put("serverInfo", serverInfo);

            response = McpRpcResult.success(jsonRpcMessage.getId(), result);
        }

        if ("notifications/initialized".equals(method)) {
            log.info("MCP客户端握手完成，sessionId: {}", sessionId);
            return;
        }

        if ("tools/list".equals(method)) {
            Map<String, Object> tool1 = new LinkedHashMap<>();
            tool1.put("name", "test_ping");
            tool1.put("description", "测试工具：返回`测试通过`");
            tool1.put("inputSchema", Collections.singletonMap("type", "object"));

            Map<String, Object> tool2 = new LinkedHashMap<>();
            tool2.put("name", "get_current_time");
            tool2.put("description", "获取服务器当前时间");
            tool2.put("inputSchema", Collections.singletonMap("type", "object"));

            Map<String, Object> tool3 = new LinkedHashMap<>();
            tool3.put("name", "echo");
            tool3.put("description", "回声工具：复读输入内容");

            Map<String, Object> echoSchema = new LinkedHashMap<>();
            echoSchema.put("type", "object");
            Map<String, Object> echoProps = new LinkedHashMap<>();
            echoProps.put("message", Collections.singletonMap("type", "string"));
            echoSchema.put("properties", echoProps);
            tool3.put("inputSchema", echoSchema);

            Map<String, Object> tool4 = new LinkedHashMap<>();
            tool4.put("name", "count_chars");
            tool4.put("description", "统计输入文本的字符数");

            Map<String, Object> countSchema = new LinkedHashMap<>();
            countSchema.put("type", "object");
            Map<String, Object> countProps = new LinkedHashMap<>();
            countProps.put("text", Collections.singletonMap("type", "string"));
            countSchema.put("properties", countProps);
            tool4.put("inputSchema", countSchema);

            Map<String, Object> result = new LinkedHashMap<>();
            result.put("tools", List.of(tool1, tool2, tool3, tool4));
            response = McpRpcResult.success(jsonRpcMessage.getId(), result);
        }

        if ("tools/call".equals(method)) {
            Map<String, Object> contentItem = new LinkedHashMap<>();
            contentItem.put("type", "text");

            String toolName = null;
            Map<String, Object> arguments = null;
            if (jsonRpcMessage.getParams() != null) {
                toolName = (String) jsonRpcMessage.getParams().get("name");
                Object argsObj = jsonRpcMessage.getParams().get("arguments");
                if (argsObj instanceof Map) {
                    arguments = (Map<String, Object>) argsObj;
                }
            }

            if ("test_ping".equals(toolName)) {
                contentItem.put("text", "测试通过");
            }

            if ("get_current_time".equals(toolName)) {
                contentItem.put("text", new Date().toString());
            }

            if ("echo".equals(toolName)) {
                String input = arguments != null ? (String) arguments.getOrDefault("message", "") : "";
                contentItem.put("text", "Echo: " + input);
            }

            if ("count_chars".equals(toolName)) {
                String input = arguments != null ? (String) arguments.getOrDefault("text", "") : "";
                contentItem.put("text", "字符数: " + input.length());
            }

            if (contentItem.get("text") == null) {
                contentItem.put("text", "未知工具: " + toolName);
            }

            Map<String, Object> result = new LinkedHashMap<>();
            result.put("content", Collections.singletonList(contentItem));
            response = McpRpcResult.success(jsonRpcMessage.getId(), result);
        }

        if (response == null) {
            response = McpRpcResult.error(
                    jsonRpcMessage.getId(),
                    McpRpcResult.McpErrorCode.METHOD_NOT_FOUND,
                    "Method not found: " + method);
        }

        try {
            emitter.send(SseEmitter.event().data(gson.toJson(response)));
        } catch (IOException e) {
            log.error("发送SSE响应失败", e);
        }
    }
}
