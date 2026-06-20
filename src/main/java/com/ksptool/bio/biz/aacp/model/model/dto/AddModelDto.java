package com.ksptool.bio.biz.aacp.model.model.dto;

import com.ksptool.bio.biz.core.common.aop.DtoCustomValidator;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Range;

import java.time.LocalDateTime;

@Getter
@Setter
public class AddModelDto implements DtoCustomValidator {

    @NotBlank(message = "模型变体名称不能为空")
    @Size(max = 80, message = "模型变体名称不能超过80个字符")
    @Schema(description = "模型变体名称")
    private String name;

    @NotBlank(message = "模型标识不能为空")
    @Size(max = 64, message = "模型标识不能超过64个字符")
    @Schema(description = "模型标识")
    private String code;

    @NotNull(message = "类型不能为空")
    @Range(min = 0, max = 3, message = "类型值无效，0:文本 1:图形 2:音频 3:多模态")
    @Schema(description = "类型 0:文本 1:图形 2:音频 3:多模态")
    private Integer kind;

    @NotNull(message = "最大上下文长度不能为空")
    @Range(min = 1, message = "最大上下文长度必须大于0")
    @Schema(description = "最大上下文长度")
    private Integer maxContext;

    @NotNull(message = "最大输出词元不能为空")
    @Range(min = 1, message = "最大输出词元必须大于0")
    @Schema(description = "最大输出词元")
    private Integer maxOutputToken;

    @NotNull(message = "推理不能为空")
    @Range(min = 0, max = 1, message = "推理值无效，0:不支持 1:支持")
    @Schema(description = "推理 0:不支持 1:支持")
    private Integer apiReasoning;

    @NotNull(message = "推理强度不能为空")
    @Range(min = 0, max = 4, message = "推理强度值无效，0:关 1:低 2:中 3:高 4:极高")
    @Schema(description = "推理强度 0:关 1:低 2:中 3:高 4:极高")
    private Integer apiReasoningEffort;

    @Schema(description = "附加参数")
    private String apiAppendParam;

    @Schema(description = "附加请求头")
    private String apiAppendHeaders;

    @NotBlank(message = "输入单价不能为空")
    @Schema(description = "输入单价")
    private String fincInput;

    @NotBlank(message = "输入单价(缓存)不能为空")
    @Schema(description = "输入单价(缓存)")
    private String fincInputCached;

    @NotBlank(message = "输出单价不能为空")
    @Schema(description = "输出单价")
    private String fincOutput;

    @Size(max = 200, message = "备注不能超过200个字符")
    @Schema(description = "备注")
    private String remark;

    @NotNull(message = "排序不能为空")
    @Schema(description = "排序")
    private Integer seq;

    @NotNull(message = "状态不能为空")
    @Range(min = 0, max = 1, message = "状态值无效，0:禁用 1:启用")
    @Schema(description = "状态 0:禁用 1:启用")
    private Integer status;

    @Override
    public String validate() {
        return null;
    }
}