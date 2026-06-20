package com.ksptool.bio.biz.assembly.model.scm.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CopyScmDto {

    @NotNull(message = "源SCM ID不能为空")
    @Schema(description = "源SCM ID")
    private Long id;

    @NotBlank(message = "SCM名称不能为空")
    @Size(max = 32, message = "SCM名称长度不能超过32个字符")
    @Schema(description = "SCM名称")
    private String name;

}