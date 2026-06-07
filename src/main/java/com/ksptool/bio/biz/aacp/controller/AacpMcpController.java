package com.ksptool.bio.biz.aacp.controller;

import com.ksptool.assembly.entity.web.CommonIdDto;
import com.ksptool.assembly.entity.web.PageResult;
import com.ksptool.assembly.entity.web.Result;
import com.ksptool.bio.biz.aacp.model.dto.AddAacpMcpDto;
import com.ksptool.bio.biz.aacp.model.dto.EditAacpMcpDto;
import com.ksptool.bio.biz.aacp.model.dto.GetAacpMcpListDto;
import com.ksptool.bio.biz.aacp.model.dto.McpRpcDto;
import com.ksptool.bio.biz.aacp.model.vo.GetAacpMcpDetailsVo;
import com.ksptool.bio.biz.aacp.model.vo.GetAacpMcpListVo;
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
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
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

        // 解析 jsonRpcMessage，分发到具体的业务逻辑 (见下文)
        String responseMessage = "";

        try {
            // 通过 SSE 通道将响应发回给客户端
            emitter.send(SseEmitter.event().data(responseMessage));
        } catch (IOException e) {
            log.error("Failed to send response", e);
        }
    }

}
