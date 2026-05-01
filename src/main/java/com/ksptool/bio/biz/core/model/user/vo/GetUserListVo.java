package com.ksptool.bio.biz.core.model.user.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class GetUserListVo {

    @Schema(description = "用户ID")
    private Long id;

    @Schema(description = "所属租户ID")
    private Long rootId;

    @Schema(description = "所属租户名称")
    private String rootName;

    @Schema(description = "所属企业ID")
    private Long orgId;

    @Schema(description = "所属企业名称")
    private String orgName;

    @Schema(description = "所属部门ID")
    private Long deptId;

    @Schema(description = "所属部门名称")
    private String deptName;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "用户昵称")
    private String nickname;

    @Schema(description = "用户性别")
    private Integer gender;

    @Schema(description = "用户手机号")
    private String phone;

    @Schema(description = "用户邮箱")
    private String email;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "最后登录时间")
    private LocalDateTime lastLoginTime;

    @Schema(description = "用户状态 0:正常 1:封禁")
    private Integer status;

    @Schema(description = "是否为系统内置用户 0:否 1:是")
    private Integer isSystem;
}
