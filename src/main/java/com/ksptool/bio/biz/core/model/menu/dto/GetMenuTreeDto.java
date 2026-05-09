package com.ksptool.bio.biz.core.model.menu.dto;


import com.ksptool.bio.biz.core.common.Switch;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class GetMenuTreeDto {

    @Schema(description = "菜单名称(模糊)")
    private String name;

    @Schema(description = "菜单类型 0:目录 1:菜单 2:按钮")
    private Integer kind;

    @Schema(description = "权限码")
    private String permissionCode;

}
