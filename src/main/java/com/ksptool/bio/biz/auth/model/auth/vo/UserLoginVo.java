package com.ksptool.bio.biz.auth.model.auth.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Set;

@Getter
@Setter
public class UserLoginVo {

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "用户昵称")
    private String nickname;

    @Schema(description = "用户性别 0:男 1:女 2:不愿透露")
    private Integer gender;

    @Schema(description = "用户手机号")
    private String phone;

    @Schema(description = "用户邮箱")
    private String email;

    @Schema(description = "用户状态 0:正常 1:封禁")
    private Integer status;

    @Schema(description = "最后登录时间 格式:yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastLoginTime;

    @Schema(description = "用户头像附件ID")
    private Long avatarAttachId;

    @Schema(description = "AUS字段: 租户ID")
    private Long rootId;

    @Schema(description = "AUS字段: 直属企业ID")
    private Long orgId;

    @Schema(description = "AUS字段: 直属部门ID")
    private Long deptId;

    @Schema(description = "AUS字段: 租户名称")
    private String rootName;

    @Schema(description = "AUS字段: 直属企业名称")
    private String orgName;

    @Schema(description = "AUS字段: 直属部门名称")
    private String deptName;

    @Schema(description = "是否为系统内置用户 0:否 1:是")
    private Integer isSystem;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "用户会话ID")
    private String sessionId;

    @Schema(description = "权限码")
    private Set<String> authorities;

    @Schema(description = "应用版本号")
    private String appVersion;

    @Schema(description = "应用版本号(数字化)")
    private String appVersionNumeric;
}
