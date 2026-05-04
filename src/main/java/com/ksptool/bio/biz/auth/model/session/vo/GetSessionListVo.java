package com.ksptool.bio.biz.auth.model.session.vo;

import com.ksptool.bio.biz.auth.common.RowScopes;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class GetSessionListVo {

    @Schema(description = "会话ID")
    private Long id;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "权限节点数量")
    private Integer permissionCount;

    @Schema(description = "登入时间")
    private LocalDateTime createTime;

    @Schema(description = "最大RowScope等级 0:全集团 10:本公司+下级公司 20:仅本公司 30:本部门+下级部门 40:仅本部门 50:仅本人 60:指定组织")
    private RowScopes rsMax;

    @Schema(description = "过期时间")
    private LocalDateTime expiresAt;

}
