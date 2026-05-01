package com.ksptool.bio.biz.core.model.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Range;

import java.util.List;

@Getter
@Setter
public class BatchEditUserDto {

    @Schema(description = "用户ID列表")
    @NotEmpty(message = "用户ID列表不能为空")
    private List<Long> ids;

    @Schema(description = "批量操作类型 0:启用 1:封禁 2:删除 3:变更组织架构")
    @NotNull(message = "批量操作类型不能为空")
    @Range(min = 0, max = 3, message = "批量操作类型只能在0-3之间")
    private Integer kind;

    @Schema(description = "变更组织架构ID 当kind为3时必填 允许为公司、子公司、部门")
    private Long orgId;

}
