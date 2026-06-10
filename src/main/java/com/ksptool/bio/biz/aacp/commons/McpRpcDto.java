package com.ksptool.bio.biz.aacp.commons;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

/**
 * JSON-RPC 2.0 请求体
 */
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
    private Map<String, Object> params;
}
