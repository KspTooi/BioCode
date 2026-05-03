package com.ksptool.bio.biz.core.model.user;

import com.ksptool.assembly.entity.exception.AuthException;
import com.ksptool.bio.biz.auth.common.aop.RowScopePo;
import com.ksptool.bio.biz.auth.service.SessionService;
import com.ksptool.bio.biz.core.common.Switch;
import com.ksptool.bio.biz.core.common.jpa.SnowflakeIdGenerated;
import com.ksptool.bio.biz.core.model.attach.AttachPo;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "core_user", comment = "用户表", uniqueConstraints = {
        @UniqueConstraint(name = "uk_user_username", columnNames = {"username"})
})
@Getter
@Setter
@SQLDelete(sql = "UPDATE core_user SET delete_time = NOW() WHERE id = ?")
@SQLRestriction("delete_time IS NULL")
public class UserPo extends RowScopePo{

    @Id
    @SnowflakeIdGenerated
    @Column(name = "id", comment = "用户ID")
    private Long id;

    @Column(name = "root_id", nullable = false, comment = "租户ID")
    private Long rootId;

    @Column(name = "org_id", comment = "直属企业ID")
    private Long orgId;

    @Column(name = "dept_id", comment = "直属部门ID")
    private Long deptId;

    @Column(name = "username", nullable = false, length = 80, comment = "用户名")
    private String username;

    @Column(name = "password", nullable = false, length = 1280, comment = "密码")
    private String password;

    @Column(name = "nickname", length = 80, comment = "昵称")
    private String nickname;

    @Column(name = "gender", columnDefinition = "tinyint", comment = "性别 0:男 1:女 2:不愿透露")
    private Integer gender;

    @Column(name = "phone", length = 20, comment = "手机号")
    private String phone;

    @Column(name = "email", length = 128, comment = "邮箱")
    private String email;

    @Column(name = "login_count", nullable = false, comment = "登录次数")
    private Integer loginCount;

    @Column(name = "status", columnDefinition = "tinyint", nullable = false, comment = "用户状态 0:封禁 1:正常")
    private Integer status;

    @Column(name = "last_login_time", comment = "最后登录时间")
    private LocalDateTime lastLoginTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "avatar_attach_id", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT), comment = "用户头像附件")
    private AttachPo avatarAttach;

    @Column(name = "is_system", columnDefinition = "tinyint", nullable = false, comment = "系统内置用户 0:否 1:是")
    private Integer isSystem;

    @Column(name = "data_version", nullable = false, comment = "数据版本")
    private Long dataVersion;

    @CreatedDate
    @Column(name = "create_time", nullable = false, updatable = false, comment = "创建时间")
    private LocalDateTime createTime;

    @CreatedBy
    @Column(name = "creator_id", nullable = false, updatable = false, comment = "创建者ID")
    private Long creatorId;

    @LastModifiedDate
    @Column(name = "update_time", nullable = false, comment = "修改时间")
    private LocalDateTime updateTime;

    @LastModifiedBy
    @Column(name = "updater_id", nullable = false, comment = "修改者ID")
    private Long updaterId;

    @Column(name = "delete_time", comment = "删除时间 为null代表未删除")
    private LocalDateTime deleteTime;


    @PrePersist
    public void prePersist() throws AuthException {

        if (loginCount == null) {
            loginCount = 0;
        }

        if (status == null) {
            status = 0;
        }

        if (dataVersion == null) {
            dataVersion = 0L;
        }

        if(rootId == null){
            rootId = SessionService.session().getRootId();
        }

    }

    @PreUpdate
    public void preUpdate() throws AuthException {

    }

    /**
     * 判断是否为内置用户
     *
     * @return 是否为内置用户
     */
    public boolean isSystem() {
        return isSystem == Switch.yes();
    }

    /**
     * 获取最细粒度的组织架构ID
     * 如果用户有直属部门则返回直属部门ID，否则返回直属企业ID
     *
     * @return 最细粒度的组织架构ID
     */
    public Long getMinOrgId() {
        if(deptId != null){
            return deptId;
        }
        return orgId;
    }

}
