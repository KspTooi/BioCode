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

    // 智能体枢纽编码
    private String serverCode;

    // 智能体枢纽ID
    private Long serverId;

    // 智能体枢纽名称
    private String serverName;

    // 连接时间
    private LocalDateTime connectTime;

    // 状态 0:初始化 1:活跃
    private int status;

    // 总请求次数
    private long inboundCount;

    // SSE 连接
    private SseEmitter emitter;

    public McpClientSession(String sessionId, String serverCode, Long serverId, String serverName, SseEmitter emitter) {
        this.sessionId = sessionId;
        this.serverCode = serverCode;
        this.serverId = serverId;
        this.serverName = serverName;
        this.emitter = emitter;
        this.connectTime = LocalDateTime.now();
        this.status = 0;
        this.inboundCount = 0;
    }
}
