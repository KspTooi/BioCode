package com.ksptool.bio.biz.assembly.model.opschema.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CopyOpSchemaDto {

    @NotNull(message = "源输出方案ID不能为空")
    @Schema(description = "源输出方案ID")
    private Long id;

    @NotBlank(message = "输出方案名称不能为空")
    @Size(max = 32, message = "输出方案名称长度不能超过32个字符")
    @Schema(description = "输出方案名称")
    private String name;

}