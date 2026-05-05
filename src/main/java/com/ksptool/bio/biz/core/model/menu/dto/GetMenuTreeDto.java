package com.ksptool.bio.biz.core.model.menu.dto;


import com.ksptool.bio.biz.core.common.Switch;

import io.swagger.v3.oas.annotations.media.Schema;
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

    @Schema(description = "是否查询可授予GM 0:否 1:是 可授予菜单是用户当前拥有的菜单，用户只能授予自己拥有的菜单")
    private Switch grantable;

}
