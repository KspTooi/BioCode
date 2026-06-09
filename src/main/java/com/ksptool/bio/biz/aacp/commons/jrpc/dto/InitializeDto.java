package com.ksptool.bio.biz.aacp.commons.jrpc.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

/**
 * initialize 方法参数：客户端声明协议版本与能力
 */
@Getter
@Setter
@Schema(description = "MCP 初始化参数")
public class InitializeDto {

    @Schema(description = "协议版本")
    private String protocolVersion;

    @Schema(description = "客户端能力声明")
    private Capabilities capabilities;

    @Schema(description = "客户端信息")
    private ClientInfo clientInfo;

    @Getter
    @Setter
    @Schema(description = "客户端能力")
    public static class Capabilities {

        @Schema(description = "引导能力")
        private Map<String, Object> elicitation;

        @Schema(description = "根URI能力")
        private Map<String, Object> roots;

        @Schema(description = "扩展能力")
        private Map<String, Object> extensions;
    }

    @Getter
    @Setter
    @Schema(description = "客户端信息")
    public static class ClientInfo {

        @Schema(description = "客户端名称")
        private String name;

        @Schema(description = "客户端版本")
        private String version;
    }
}
