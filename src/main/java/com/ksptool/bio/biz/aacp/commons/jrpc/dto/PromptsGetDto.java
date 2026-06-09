package com.ksptool.bio.biz.aacp.commons.jrpc.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

/**
 * prompts/get 方法参数：提示词名 + 参数
 */
@Getter
@Setter
@Schema(description = "获取提示词参数")
public class PromptsGetDto {

    @Schema(description = "提示词名称")
    private String name;

    @Schema(description = "提示词参数")
    private Map<String, Object> arguments;
}
