package com.ksptool.bio.biz.assembly.model.datsource.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CopyDataSourceDto {

    @NotNull(message = "源数据源ID不能为空")
    @Schema(description = "源数据源ID")
    private Long id;

    @NotBlank(message = "数据源名称不能为空")
    @Size(max = 32, min = 1, message = "数据源名称长度必须在1-32个字符之间")
    @Schema(description = "数据源名称")
    private String name;

    @NotBlank(message = "数据源编码不能为空")
    @Size(max = 32, min = 1, message = "数据源编码长度必须在1-32个字符之间")
    @Schema(description = "数据源编码")
    private String code;

}