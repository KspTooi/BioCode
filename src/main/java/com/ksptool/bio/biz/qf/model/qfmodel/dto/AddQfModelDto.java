package com.ksptool.bio.biz.qf.model.qfmodel.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

@Getter
@Setter
public class AddQfModelDto {

    @Schema(description = "模型分组ID")
    private Long groupId;

    @Schema(description = "关联表单ID")
    private Long formId;

    @NotNull(message = "模型名称不能为空")
    @Length(max = 80, message = "模型名称长度不能超过80个字符")
    @Schema(description = "模型名称")
    private String name;

    @NotNull(message = "模型编码不能为空")
    @Length(max = 32, message = "模型编码长度不能超过32个字符")
    @Schema(description = "模型编码")
    private String code;

    @NotNull(message = "排序不能为空")
    @Min(value = 0, message = "排序必须大于等于0")
    @Schema(description = "排序")
    private Integer seq;

}
