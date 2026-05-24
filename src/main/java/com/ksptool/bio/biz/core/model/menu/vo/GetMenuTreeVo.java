package com.ksptool.bio.biz.core.model.menu.vo;

import com.ksptool.bio.biz.core.common.TreeBuilder;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Set;

@Getter
@Setter
public class GetMenuTreeVo implements TreeBuilder.TreeNode<GetMenuTreeVo> {

    @Schema(description = "菜单ID")
    private Long id;

    @Schema(description = "父级ID null:根节点")
    private Long parentId;

    @Schema(description = "菜单名称")
    private String name;

    @Schema(description = "菜单图标")
    private String icon;

    @Schema(description = "菜单类型 0:目录 1:菜单 2:按钮 ,3:外链嵌套,4:外链跳转")
    private Integer kind;

    @Schema(description = "菜单路径(目录不能填写)")
    private String path;

    @Schema(description = "是否隐藏 0:否 1:是")
    private Integer hide;

    @Schema(description = "权限码列表")
    private Set<String> permissionCode;

    @Schema(description = "排序")
    private Integer seq;

    @Schema(description = "子菜单")
    private List<GetMenuTreeVo> children;

}
