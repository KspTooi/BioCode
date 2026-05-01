package com.ksptool.bio.biz.auth.model.group.dto;

import com.ksptool.bio.biz.core.common.aop.DtoCustomValidator;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

import java.util.List;

@Getter
@Setter
public class EditGroupDto implements DtoCustomValidator{

    @Schema(description = "组ID")
    @NotNull(message = "组ID不能为空")
    private Long id;

    @Schema(description = "组标识")
    @NotBlank(message = "组标识不能为空")
    @Length(min = 2, max = 32, message = "组标识长度必须在2-32个字符之间")
    @Pattern(regexp = "^[a-zA-Z][a-zA-Z_]*$", message = "组标识只能包含英文字符和下划线，且必须以字母开头")
    private String code;

    @Schema(description = "组名称")
    @NotBlank(message = "组名称不能为空")
    @Length(min = 2, max = 80, message = "组名称长度必须在2-80个字符之间")
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

    @Schema(description = "数据权限 0:全部 10:本公司/租户及以下 20:本部门及以下 30:本部门 40:仅本人 50:指定部门 60:指定部门及以下")
    @NotNull(message = "数据权限不能为空")
    @Min(value = 0, message = "数据权限不正确")
    @Max(value = 60, message = "数据权限不正确")
    private Integer rowScope;

    @NotNull(message = "部门ID列表不能为空")
    @Schema(description = "部门ID列表")
    private List<Long> deptIds;

    @Schema(description = "权限ID列表")
    private List<Long> permissionIds;

    /**
     * 验证入参
     *
     * @return 错误信息 为空则验证通过
     */
    @Override
    public String validate() {

        //数据权限只能是0、10、20、30、40、50、60
        if (this.rowScope != 0 && this.rowScope != 10 && this.rowScope != 20 && this.rowScope != 30 && this.rowScope != 40 && this.rowScope != 50 && this.rowScope != 60) {
            return "RS数据权限等级不正确";
        }

        //当数据权限为5(指定部门)时，部门ID列表不能为空
        if (this.rowScope == 5) {
            if (this.deptIds.isEmpty()) {
                return "部门ID列表不能为空";
            }
        }

        return null;
    }
}
