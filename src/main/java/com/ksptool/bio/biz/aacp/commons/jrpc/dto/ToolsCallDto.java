package com.ksptool.bio.biz.aacp.commons.jrpc.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

/**
 * tools/call 方法参数：指定工具名与入参
 */
@Getter
@Setter
@Schema(description = "工具调用参数")
public class ToolsCallDto {

    @Schema(description = "工具名称")
    private String name;

    @Schema(description = "工具入参")
    private Map<String, Object> arguments;
}
