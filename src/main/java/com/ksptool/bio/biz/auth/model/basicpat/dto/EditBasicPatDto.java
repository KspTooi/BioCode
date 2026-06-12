package com.ksptool.bio.biz.auth.model.basicpat.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import org.hibernate.validator.constraints.Range;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EditBasicPatDto {

    @NotNull(message = "主键ID不能为空")
    @Schema(description = "主键ID")
    private Long id;

    @NotNull(message = "PAT名称不能为空")
    @Size(max = 40, message = "PAT名称长度不能超过40")
    @Schema(description = "PAT名称")
    private String name;

    @NotNull(message = "状态不能为空")
    @Range(min = 0, max = 1, message = "状态必须为0或1")
    @Schema(description = "状态: 0:禁用 1:启用")
    private Integer status;

}
