package com.ksptool.bio.biz.auth.model.group.dto;

import com.ksptool.bio.biz.auth.common.RowScopes;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SimulateRsDto {

    @Schema(description = "模拟用户所在的组织节点ID(企业/子企业/部门/班组任意kind)")
    @NotNull(message = "组织节点ID不能为空")
    private Long orgId;

    @Schema(description = "模拟的RS等级 0:全集团 10:本公司+下级公司 20:仅本公司 30:本部门+下级部门 40:仅本部门 50:仅本人 100:拒绝所有 (不支持60:指定组织)")
    @NotNull(message = "RS等级不能为空")
    private RowScopes rsLevel;

}
