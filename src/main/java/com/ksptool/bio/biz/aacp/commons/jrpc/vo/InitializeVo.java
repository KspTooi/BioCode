package com.ksptool.bio.biz.aacp.commons.jrpc.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

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
    private ServerCapabilities capabilities;

    @Schema(description = "服务端信息")
    private ServerInfo serverInfo;

    @Getter
    @Setter
    @Schema(description = "服务端能力详情")
    public static class ServerCapabilities {

        @Schema(description = "Tools 能力支持")
        private CapabilityListChanged tools;

        @Schema(description = "Prompts 能力支持")
        private CapabilityListChanged prompts;

        @Schema(description = "Resources 能力支持")
        private ResourcesCapability resources;
    }

    @Getter
    @Setter
    @Schema(description = "基础能力支持（含列表变更通知）")
    public static class CapabilityListChanged {
        
        @Schema(description = "是否支持列表变更通知 (客户端可通过此标志得知服务端是否会发送 list_changed 通知)")
        private Boolean listChanged;
    }

    @Getter
    @Setter
    @Schema(description = "Resources 能力详情")
    public static class ResourcesCapability extends CapabilityListChanged {
        
        @Schema(description = "是否支持资源订阅 (允许客户端订阅指定 URI 的资源变更)")
        private Boolean subscribe;
    }

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