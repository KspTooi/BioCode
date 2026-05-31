package com.ksptool.bio.biz.qf.model.qfmodel.dto;

import lombok.Getter;
import lombok.Setter;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import org.hibernate.validator.constraints.Length;

@Getter
@Setter
public class EditQfModelDto {

    @Schema(description="模型分组ID")
    private Long groupId;

    @Schema(description="关联表单ID")
    private Long formId;

    @NotNull(message = "主键ID不能为空")
    @Schema(description="主键ID")
    private Long id;

    @NotNull(message = "模型名称不能为空")
    @Length(max = 80, message = "模型名称长度不能超过80个字符")
    @Schema(description="模型名称")
    private String name;

    @NotNull(message = "排序不能为空")
    @Min(value = 0, message = "排序必须大于等于0")
    @Schema(description="排序")
    private Integer seq;

}
