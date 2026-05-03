package com.ksptool.bio.biz.assembly.model.scm;

import com.ksptool.assembly.entity.exception.AuthException;
import com.ksptool.bio.biz.auth.common.aop.RowScopeUserOnlyPo;
import com.ksptool.bio.biz.core.common.jpa.SnowflakeIdGenerated;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "assembly_scm", indexes = {
        @Index(name = "uk_scm_name", columnList = "name", unique = true)
})
@EntityListeners(AuditingEntityListener.class)
public class ScmPo extends RowScopeUserOnlyPo {

    @Id
    @SnowflakeIdGenerated
    @Column(name = "id", nullable = false, comment = "主键ID")
    private Long id;

    @Column(name = "name", nullable = false, length = 32, comment = "SCM名称(唯一)")
    private String name;

    @Column(name = "project_name", length = 80, comment = "项目名称")
    private String projectName;

    @Column(name = "scm_url", nullable = false, length = 1000, comment = "SCM仓库地址")
    private String scmUrl;

    @Column(name = "scm_auth_kind", nullable = false, columnDefinition = "TINYINT", comment = "SCM认证方式 0:公开 1:账号密码 2:SSH KEY 3:PAT")
    private Integer scmAuthKind;

    @Column(name = "scm_username", columnDefinition = "TEXT", comment = "SCM用户名")
    private String scmUsername;

    @Column(name = "scm_password", columnDefinition = "TEXT", comment = "SCM密码")
    private String scmPassword;

    @Column(name = "scm_pk", columnDefinition = "TEXT", comment = "SSH KEY")
    private String scmPk;

    @Column(name = "scm_branch", nullable = false, length = 80, comment = "SCM分支")
    private String scmBranch;

    @Column(name = "remark", columnDefinition = "TEXT", comment = "SCM备注")
    private String remark;

    @CreatedDate
    @Column(name = "create_time", nullable = false, comment = "创建时间")
    private LocalDateTime createTime;

    @CreatedBy
    @Column(name = "creator_id", nullable = false, comment = "创建人ID")
    private Long creatorId;

    @LastModifiedDate
    @Column(name = "update_time", nullable = false, comment = "更新时间")
    private LocalDateTime updateTime;

    @LastModifiedBy
    @Column(name = "updater_id", nullable = false, comment = "更新人ID")
    private Long updaterId;


    @PrePersist
    private void onCreate() throws AuthException {


    }

    @PreUpdate
    private void onUpdate() throws AuthException {

    }
}
