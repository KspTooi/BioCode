package com.ksptool.bio.biz.aacp.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@Schema(description = "JSON-RPC 2.0 请求体")
public class McpRpcDto {

    @Schema(description = "JSON-RPC版本号")
    private String jsonrpc;

    @Schema(description = "请求ID")
    private Integer id;

    @Schema(description = "方法名")
    private String method;

    @Schema(description = "请求参数")
    private McpInitializeParamsDto params;

    @Getter
    @Setter
    @Schema(description = "initialize 方法参数")
    public static class McpInitializeParamsDto {

        @Schema(description = "协议版本")
        private String protocolVersion;

        @Schema(description = "客户端能力声明")
        private McpCapabilitiesDto capabilities;

        @Schema(description = "客户端信息")
        private McpClientInfoDto clientInfo;

        @Getter
        @Setter
        @Schema(description = "能力声明")
        public static class McpCapabilitiesDto {

            @Schema(description = "Elicitation 能力")
            private Map<String, Object> elicitation;

            @Schema(description = "Roots 能力")
            private Map<String, Object> roots;

            @Schema(description = "扩展能力")
            private Map<String, Object> extensions;
        }

        @Getter
        @Setter
        @Schema(description = "客户端信息")
        public static class McpClientInfoDto {

            @Schema(description = "客户端名称")
            private String name;

            @Schema(description = "客户端版本")
            private String version;
        }
    }
}
