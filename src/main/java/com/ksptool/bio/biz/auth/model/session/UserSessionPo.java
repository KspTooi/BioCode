package com.ksptool.bio.biz.auth.model.session;

import com.ksptool.bio.biz.auth.common.RowScopes;
import com.ksptool.bio.biz.auth.common.aop.RowScopePo;
import com.ksptool.bio.biz.core.common.jpa.SetLongConv;
import com.ksptool.bio.biz.core.common.jpa.SetStringConv;
import com.ksptool.bio.biz.core.common.jpa.SnowflakeIdGenerated;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.Set;

@Getter
@Setter
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "auth_user_session", comment = "用户会话")
public class UserSessionPo extends RowScopePo {

    @Id
    @SnowflakeIdGenerated
    @Column(name = "id", comment = "会话ID")
    private Long id;

    @Column(name = "session_id", nullable = false, unique = true, length = 200, comment = "用户凭据SessionID")
    private String sessionId;

    @Column(name = "user_id", nullable = false, comment = "用户ID")
    private Long userId;

    @Column(name = "root_id", nullable = false, comment = "租户ID")
    private Long rootId;

    @Column(name = "org_id", comment = "直属企业ID")
    private Long orgId;

    @Column(name = "dept_id", comment = "直属部门ID")
    private Long deptId;

    @Column(name = "root_name", nullable = false, length = 40, comment = "租户名")
    private String rootName;

    @Column(name = "org_name", length = 80, comment = "直属企业名")
    private String orgName;

    @Column(name = "dept_name", length = 80, comment = "直属部门名")
    private String deptName;

    @Column(name = "username", nullable = false, length = 80, comment = "用户名")
    private String username;

    @Column(name = "nickname", length = 80, comment = "用户昵称")
    private String nickname;

    @Convert(converter = SetStringConv.class)
    @Column(name = "permissions", nullable = false, columnDefinition = "JSON", comment = "用户权限代码JSON")
    private Set<String> permissionCodes;

    @Column(name = "rs_max", nullable = false, columnDefinition = "TINYINT", comment = "最大RowScope等级 0:全集团 10:本公司+下级公司 20:仅本公司 30:本部门+下级部门 40:仅本部门 50:仅本人 60:指定组织")
    private RowScopes rsMax;

    @Convert(converter = SetLongConv.class)
    @Column(name = "rs_allow_org_ids", nullable = false, columnDefinition = "JSON", comment = "RowScope允许访问的组织IDS")
    private Set<Long> rsAllowOrgIds;

    @Column(name = "data_version", nullable = false, comment = "数据版本")
    private Long dataVersion;

    @Column(name = "expires_at", nullable = false, comment = "过期时间")
    private LocalDateTime expiresAt;

    @CreatedDate
    @Column(name = "create_time", nullable = false, updatable = false, comment = "创建时间")
    private LocalDateTime createTime;

    @CreatedBy
    @Column(name = "creator_id", nullable = false, updatable = false, comment = "创建者ID")
    private Long creatorId;

    @LastModifiedDate
    @Column(name = "update_time", nullable = false, comment = "修改时间")
    private LocalDateTime updateTime;


    /**
     * 判断会话是否已过期
     *
     * @return 是否过期 true:过期 false:未过期
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }


    @PrePersist
    private void onCreate() {

    }

    @PreUpdate
    private void onUpdate() {

    }


}