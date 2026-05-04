package com.ksptool.bio.biz.auth.model.session.vo;

import com.ksptool.bio.biz.auth.common.RowScopes;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Getter
@Setter
public class GetSessionDetailsVo {

    @Schema(description = "会话ID")
    private Long id;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "登入时间")
    private LocalDateTime createTime;

    @Schema(description = "过期时间")
    private LocalDateTime expiresAt;

    @Schema(description = "权限节点")
    private Set<String> permissions;

    @Schema(description = "最大RowScope等级 0:全集团 10:本公司+下级公司 20:仅本公司 30:本部门+下级部门 40:仅本部门 50:仅本人 60:指定组织")
    private RowScopes rsMax;

    @Schema(description = "RowScope允许访问的部门名称列表")
    private List<String> rsDeptNames;

}
