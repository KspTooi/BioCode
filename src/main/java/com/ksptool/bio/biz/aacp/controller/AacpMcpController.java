package com.ksptool.bio.biz.aacp.controller;

import com.google.gson.Gson;
import com.ksptool.assembly.entity.web.CommonIdDto;
import com.ksptool.assembly.entity.web.PageResult;
import com.ksptool.assembly.entity.web.Result;
import com.ksptool.bio.biz.aacp.model.dto.AddAacpMcpDto;
import com.ksptool.bio.biz.aacp.model.dto.EditAacpMcpDto;
import com.ksptool.bio.biz.aacp.model.dto.GetAacpMcpListDto;
import com.ksptool.bio.biz.aacp.model.dto.McpRpcDto;
import com.ksptool.bio.biz.aacp.model.vo.GetAacpMcpDetailsVo;
import com.ksptool.bio.biz.aacp.model.vo.GetAacpMcpListVo;
import com.ksptool.bio.biz.aacp.model.vo.McpRpcResult;
import com.ksptool.bio.biz.aacp.service.AacpMcpService;
import com.ksptool.bio.biz.auth.common.DynamicGlobalWhiteManager;
import com.ksptool.bio.commons.annotation.PrintLog;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@PrintLog
@RestController
@RequestMapping("/aacpMcp")
@Tag(name = "MCP服务器", description = "MCP服务器")
@Slf4j
public class AacpMcpController {

    // 存放 SessionID 对应的 SSE 连接
    private final Map<String, SseEmitter> sessionMap = new ConcurrentHashMap<>();

    private final Gson gson = new Gson();

    @Autowired
    private AacpMcpService aacpMcpService;

    @Autowired
    private DynamicGlobalWhiteManager dgwm;

    @EventListener(ApplicationReadyEvent.class)
    public void init() {
        log.info("AACP-VIA-BIO 正在配置MCP服务器端点白名单。");
        dgwm.getWhiteListMatchers().add(PathPatternRequestMatcher.pathPattern("/aacpMcp/upstream/**"));
        dgwm.getWhiteListMatchers().add(PathPatternRequestMatcher.pathPattern("/aacpMcp/request"));
    }


    @PreAuthorize("@auth.hasCode('aacp:mcp:view')")
    @PostMapping("/getAacpMcpList")
    @Operation(summary = "查询MCP服务器列表")
    public PageResult<GetAacpMcpListVo> getAacpMcpList(@RequestBody @Valid GetAacpMcpListDto dto) throws Exception {
        return aacpMcpService.getAacpMcpList(dto);
    }

    @PreAuthorize("@auth.hasCode('aacp:mcp:add')")
    @Operation(summary = "新增MCP服务器")
    @PostMapping("/addAacpMcp")
    public Result<String> addAacpMcp(@RequestBody @Valid AddAacpMcpDto dto) throws Exception {
        aacpMcpService.addAacpMcp(dto);
        return Result.success("新增成功");
    }

    @PreAuthorize("@auth.hasCode('aacp:mcp:edit')")
    @Operation(summary = "编辑MCP服务器")
    @PostMapping("/editAacpMcp")
    public Result<String> editAacpMcp(@RequestBody @Valid EditAacpMcpDto dto) throws Exception {
        aacpMcpService.editAacpMcp(dto);
        return Result.success("修改成功");
    }

    @PreAuthorize("@auth.hasCode('aacp:mcp:view')")
    @Operation(summary = "查询MCP服务器详情")
    @PostMapping("/getAacpMcpDetails")
    public Result<GetAacpMcpDetailsVo> getAacpMcpDetails(@RequestBody @Valid CommonIdDto dto) throws Exception {
        GetAacpMcpDetailsVo details = aacpMcpService.getAacpMcpDetails(dto);
        if (details == null) {
            return Result.error("无数据");
        }
        return Result.success(details);
    }

    @PreAuthorize("@auth.hasCode('aacp:mcp:remove')")
    @Operation(summary = "删除MCP服务器")
    @PostMapping("/removeAacpMcp")
    public Result<String> removeAacpMcp(@RequestBody @Valid CommonIdDto dto) throws Exception {
        aacpMcpService.removeAacpMcp(dto);
        return Result.success("操作成功");
    }

    /**
     * 客户端首先请求这里建立 SSE 连接
     */
    @GetMapping(value = "/upstream/{code}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter upstream(@PathVariable("code") String code, @RequestHeader(value = "Authorization", required = false) String authHeader) {

        log.info("AACP-VIA-BIO 客户端建立SSE连接，虚拟MCP编码: {}", code);

        String sessionId = UUID.randomUUID().toString();
        SseEmitter emitter = new SseEmitter(3600000L); // 设置超时

        sessionMap.put(sessionId, emitter);

        emitter.onCompletion(() -> sessionMap.remove(sessionId));
        emitter.onTimeout(() -> sessionMap.remove(sessionId));

        try {
            // 根据 MCP 规范，建立连接后，服务端必须下发 endpoint URI 给客户端
            // 告诉客户端往哪个 URL POST 消息
            emitter.send(SseEmitter.event()
                    .name("endpoint")
                    .data("/aacpMcp/request?sessionId=" + sessionId));
        } catch (IOException e) {
            sessionMap.remove(sessionId);
        }
        return emitter;
    }

    /**
     * 客户端将所有的 JSON-RPC 请求 POST 到这里
     */
    @PostMapping(value = "/request", consumes = "application/json")
    public void request(@RequestParam("sessionId") String sessionId, @RequestBody McpRpcDto jsonRpcMessage) {

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
            result.put("tools", java.util.List.of(tool1, tool2, tool3, tool4));
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
                contentItem.put("text", new java.util.Date().toString());
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
