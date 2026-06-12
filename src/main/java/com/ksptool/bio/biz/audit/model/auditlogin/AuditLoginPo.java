package com.ksptool.bio.biz.audit.model.auditlogin;

import com.ksptool.bio.biz.core.common.jpa.SnowflakeIdGenerated;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "audit_login_rcd")
public class AuditLoginPo {

    @Id
    @SnowflakeIdGenerated
    @Column(name = "id", comment = "登录日志主键")
    private Long id;

    @Column(name = "user_id", comment = "用户ID")
    private Long userId;

    @Column(name = "username", nullable = false, length = 32, comment = "用户账号")
    private String username;

    @Column(name = "login_kind", nullable = false, columnDefinition = "tinyint", comment = "登录方式 0:用户名密码 1:PAT登录")
    private Integer loginKind;

    @Column(name = "ip_addr", nullable = false, length = 32, comment = "登录 IP")
    private String ipAddr;

    @Column(name = "location", nullable = false, length = 32, comment = "IP 归属地")
    private String location;

    @Column(name = "browser", nullable = false, length = 32, comment = "浏览器/客户端指纹")
    private String browser;

    @Column(name = "os", nullable = false, length = 32, comment = "操作系统")
    private String os;

    @Column(name = "status", nullable = false, columnDefinition = "tinyint", comment = "状态: 0:成功 1:失败")
    private Integer status;

    @Column(name = "message", nullable = false, length = 255, comment = "提示消息")
    private String message;

    @CreatedDate
    @Column(name = "create_time", nullable = false, updatable = false, comment = "创建时间")
    private LocalDateTime createTime;


    @PrePersist
    private void onCreate() {


    }

    @PreUpdate
    private void onUpdate() {


    }
}
