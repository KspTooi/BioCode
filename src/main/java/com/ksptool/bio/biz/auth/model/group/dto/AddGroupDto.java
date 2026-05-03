package com.ksptool.bio.biz.auth.model.group.dto;

import com.ksptool.bio.biz.auth.common.RowScopes;
import com.ksptool.bio.biz.core.common.aop.DtoCustomValidator;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

import java.util.List;

@Getter
@Setter
public class AddGroupDto implements DtoCustomValidator{

    @Schema(description = "组编码")
    @NotBlank(message = "组编码不能为空")
    @Length(min = 2, max = 32, message = "组编码长度必须在2-32个字符之间")
    @Pattern(regexp = "^[a-zA-Z][a-zA-Z_]*$", message = "组编码只能包含英文字符和下划线，且必须以字母开头")
    private String code;

    @Schema(description = "组名称")
    @NotBlank(message = "组名称不能为空")
    @Length(min = 2, max = 80, message = "组名称长度必须在2-50个字符之间")
    private String name;

    @Schema(description = "组描述")
    @Length(max = 200, message = "组描述长度不能超过200个字符")
    private String remark;

    @Schema(description = "组状态：0-禁用，1-启用")
    @NotNull(message = "组状态不能为空")
    @Min(value = 0, message = "状态值不正确")
    @Max(value = 1, message = "状态值不正确")
    private Integer status;

    @Schema(description = "排序号")
    @NotNull(message = "排序号不能为空")
    @Min(value = 0, message = "排序号必须大于等于0")
    private Integer seq;

    @Schema(description = "RS数据权限等级 0:全集团 10:本公司+下级公司 20:仅本公司 30:本部门+下级部门 40:仅本部门 50:仅本人 60:指定组织")
    @NotNull(message = "数据权限不能为空")
    private RowScopes rowScope;

    @NotNull(message = "部门ID列表不能为空")
    @Schema(description = "部门ID列表 允许空数组但不能为NULL")
    private List<Long> deptIds;

    @NotNull(message = "权限ID列表不能为空")
    @Schema(description = "权限ID列表 允许空数组但不能为NULL")
    private List<Long> permissionIds;

    @NotNull(message = "菜单ID列表不能为空")
    @Schema(description = "菜单ID列表 允许空数组但不能为NULL")
    private List<Long> menuIds;

    /**
     * 验证入参
     *
     * @return 错误信息 为空则验证通过
     */
    @Override
    public String validate() {

        //当数据权限为指定组织时，部门ID列表不能为空
        if (this.rowScope == RowScopes.SPECIFIED_ORG) {
            if (this.deptIds.isEmpty()) {
                return "部门ID列表不能为空";
            }
        }
        return null;
    }
}
