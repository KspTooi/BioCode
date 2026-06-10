package com.ksptool.bio.biz.aacp.commons.jrpc.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

/**
 * initialize 响应：服务端信息
 */
@Getter
@Setter
@Schema(description = "MCP 初始化响应")
public class InitializeVo {

    @Schema(description = "协议版本")
    private String protocolVersion;

    @Schema(description = "服务端能力")
    private Map<String, Object> capabilities;

    @Schema(description = "服务端信息")
    private ServerInfo serverInfo;

    @Getter
    @Setter
    @Schema(description = "服务端信息")
    public static class ServerInfo {

        @Schema(description = "服务端名称")
        private String name;

        @Schema(description = "服务端版本")
        private String version;
    }
}
