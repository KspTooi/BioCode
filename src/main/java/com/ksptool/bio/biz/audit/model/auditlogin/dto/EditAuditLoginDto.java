package com.ksptool.bio.biz.audit.model.auditlogin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class EditAuditLoginDto {


    @Schema(description = "登录日志主键")
    private Long id;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "用户账号")
    private String username;

    @Schema(description = "登录方式 0:用户名密码")
    private Integer loginKind;

    @Schema(description = "登录 IP")
    private String ipAddr;

    @Schema(description = "IP 归属地")
    private String location;

    @Schema(description = "浏览器/客户端指纹")
    private String browser;

    @Schema(description = "操作系统")
    private String os;

    @Schema(description = "状态: 0:成功 1:失败")
    private Integer status;

    @Schema(description = "提示消息")
    private String message;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

}

