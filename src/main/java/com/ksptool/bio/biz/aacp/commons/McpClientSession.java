package com.ksptool.bio.biz.aacp.commons;

import lombok.Getter;
import lombok.Setter;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.LocalDateTime;

/**
 * MCP 客户端会话
 */
@Getter
@Setter
public class McpClientSession {

    // 会话ID
    private String sessionId;

    // MCP服务器编码
    private String serverCode;

    // MCP服务器ID
    private Long serverId;

    // 连接时间
    private LocalDateTime connectTime;

    // 状态 0:已连接 1:已初始化
    private int status;

    // SSE 连接
    private SseEmitter emitter;

    public McpClientSession(String sessionId, String serverCode, Long serverId, SseEmitter emitter) {
        this.sessionId = sessionId;
        this.serverCode = serverCode;
        this.serverId = serverId;
        this.emitter = emitter;
        this.connectTime = LocalDateTime.now();
        this.status = 0;
    }
}
