package com.ksptool.bio.biz.auth.model.session.vo;

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

    @Schema(description = "最大RowScope等级 0:全部 1:本公司/租户及以下 2:本部门及以下 3:本部门 4:仅本人 5:指定部门")
    private Integer rsMax;

    @Schema(description = "过期时间")
    private LocalDateTime expiresAt;

}
