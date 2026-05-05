package com.ksptool.bio.biz.core.model.pack.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.Range;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EditPackDto {

    @Schema(description = "主键ID")
    @NotNull(message = "主键ID不能为空")
    private Long id;

    @Schema(description = "菜单包名")
    @NotBlank(message = "菜单包名不能为空")
    @Size(max = 40, message = "菜单包名长度不能超过40个字符")
    private String name;

    @Schema(description = "状态 0:禁用 1:启用")
    @NotNull(message = "状态不能为空")
    @Range(min = 0, max = 1, message = "状态值只能为0或1")
    private Integer status;

    @Schema(description = "排序")
    @NotNull(message = "排序不能为空")
    private Integer seq;

    @Schema(description = "备注")
    @Size(max = 200, message = "备注长度不能超过200个字符")
    private String remark;

}
