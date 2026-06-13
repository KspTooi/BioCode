package com.ksptool.bio.biz.aacp.model.func.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

@Getter
@Setter
public class EditMicroFuncDto {

    @NotNull(message = "主键ID不能为空")
    @Schema(description = "主键ID")
    private Long id;

    @NotBlank(message = "微函数名称不能为空")
    @Length(max = 40, message = "微函数名称长度不能超过40")
    @Schema(description = "微函数名称")
    private String name;

    @NotBlank(message = "微函数标识不能为空")
    @Length(max = 32, message = "微函数标识长度不能超过32")
    @Schema(description = "微函数标识")
    private String code;

    @NotBlank(message = "意图词不能为空")
    @Length(max = 1000, message = "意图词长度不能超过1000")
    @Schema(description = "意图词")
    private String description;

    @Schema(description = "入参规范")
    private String schema;

    @NotBlank(message = "调用目标Bean不能为空")
    @Length(max = 1000, message = "调用目标Bean长度不能超过1000")
    @Schema(description = "调用目标Bean")
    private String target;

    @Length(max = 500, message = "备注长度不能超过500")
    @Schema(description = "备注")
    private String remark;

}
