package com.ksptool.bio.biz.core.model.menu.dto;

import com.ksptool.bio.biz.core.common.aop.DtoCustomValidator;
import com.ksptool.bio.commons.dataprocess.Str;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.Range;
import com.ksptool.bio.biz.auth.common.CheatPermission;
import com.ksptool.bio.biz.auth.common.PermissionCode;
import java.util.Set;

@Getter
@Setter
public class EditMenuDto implements DtoCustomValidator{

    @NotNull(message = "菜单ID不能为空")
    @Schema(description = "菜单ID")
    private Long id;

    @Schema(description = "父级ID -1:根节点")
    private Long parentId;

    @NotBlank(message = "菜单名称不能为空")
    @Length(min = 2, max = 40, message = "菜单名称长度必须在2-40个字符之间")
    @Schema(description = "菜单名称")
    private String name;

    @NotNull(message = "菜单类型不能为空")
    @Range(min = 0, max = 2, message = "菜单类型只能在0-2之间")
    @Schema(description = "菜单类型 0:目录 1:菜单 2:按钮")
    private Integer kind;

    @Length(max = 512, message = "菜单路径长度不能超过512个字符")
    @Schema(description = "菜单路径(目录不能填写)")
    private String path;

    @Length(max = 80, message = "菜单图标长度不能超过80个字符")
    @Schema(description = "菜单图标")
    private String icon;

    @NotNull(message = "是否隐藏不能为空")
    @Range(min = 0, max = 1, message = "是否隐藏只能在0-1之间")
    @Schema(description = "是否隐藏 0:否 1:是")
    private Integer hide;

    @Schema(description = "权限码列表")
    private Set<String> permissionCode;

    @Range(min = 0, max = 655350, message = "排序只能在0-655350之间")
    @Schema(description = "排序")
    private Integer seq;

    @Length(max = 200, message = "备注长度不能超过200个字符")
    @Schema(description = "备注")
    private String remark;

    /**
     * 验证菜单与按钮是否合法
     *
     * @return 错误信息 无错误返回null
     */
    @Override
    public String validate() {

        //菜单中不能配CheatPermission中的权限码
        if(permissionCode != null){

            if (permissionCode.size() > 10) {
                return "一个菜单最多只能增加10个权限";
            }

            for(var cCode : CheatPermission.values()){

                for(var mCode : permissionCode){
                    if(mCode.equalsIgnoreCase(cCode.getCode())){
                        return "权限码[" + cCode.getName() + "]不允许在菜单中使用！";
                    }
                }

            }

        }

        //0:目录
        if (kind == 0) {

            //为目录时不能填写的字段
            if (Str.isNotBlank(path)) {
                return "目录不支持填写路径";
            }

            //2026-04-12 可以支持多级目录
            // if (parentId != null) {
            //     return "目录仅能放置于根节点";
            // }

            //为目录时必须填写的字段
            if (Str.isBlank(name)) {
                return "目录名称不能为空";
            }
            if (Str.isBlank(icon)) {
                return "目录图标不能为空";
            }

        }

        //1:菜单
        if (kind == 1) {

            //为菜单时必须填写的字段
            if (Str.isBlank(path)) {
                return "菜单路径不能为空";
            }
            if (Str.isBlank(icon)) {
                return "菜单图标不能为空";
            }

        }

        //2:按钮
        if (kind == 2) {

            //按钮必须有父级
            if (parentId == null) {
                return "按钮必须有父级";
            }

            //为按钮时不能填写的字段
            if (Str.isNotBlank(path)) {
                return "按钮不支持填写路径";
            }
            if (Str.isNotBlank(icon)) {
                return "按钮不支持填写图标";
            }
            if (hide != null && hide == 1) {
                return "按钮不支持隐藏";
            }

        }

        return null;
    }

}
