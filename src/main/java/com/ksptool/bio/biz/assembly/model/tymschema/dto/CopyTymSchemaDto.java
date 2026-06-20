package com.ksptool.bio.biz.assembly.model.tymschema.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CopyTymSchemaDto {

    @NotNull(message = "源类型映射方案ID不能为空")
    @Schema(description = "源类型映射方案ID")
    private Long id;

    @NotBlank(message = "方案名称不能为空")
    @Size(max = 32, min = 1, message = "方案名称长度必须在1-32个字符之间")
    @Schema(description = "方案名称")
    private String name;

    @NotBlank(message = "方案编码不能为空")
    @Size(max = 32, min = 1, message = "方案编码长度必须在1-32个字符之间")
    @Schema(description = "方案编码")
    private String code;

}