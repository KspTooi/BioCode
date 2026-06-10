package com.ksptool.bio.biz.aacp.commons.jrpc.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

/**
 * completion/complete 方法参数：补全引用与参数值
 */
@Getter
@Setter
@Schema(description = "自动补全参数")
public class CompletionCompleteDto {

    @Schema(description = "补全引用对象")
    private Map<String, Object> ref;

    @Schema(description = "参数名")
    private String argument;

    @Schema(description = "已输入部分")
    private String value;
}
