package com.ksptool.bio.biz.assembly.model.polytemplate.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Range;

@Getter
@Setter
public class EditPolyTemplateDto {

    @NotBlank(message = "模板名称不能为空")
    @Size(max = 40, min = 1, message = "模板名称长度必须在1-40个字符之间")
    @Schema(description = "模板名称")
    private String name;

    @NotBlank(message = "模板代码不能为空")
    @Size(max = 16, min = 1, message = "模板代码长度必须在1-16个字符之间")
    @Schema(description = "模板代码")
    private String code;

    @NotNull(message = "排序不能为空")
    @Range(min = 0, message = "排序不能小于0")
    @Schema(description = "排序")
    private Integer seq;

    @NotNull(message = "状态不能为空")
    @Schema(description = "状态 0:禁用 1:启用")
    private Integer status;

}
