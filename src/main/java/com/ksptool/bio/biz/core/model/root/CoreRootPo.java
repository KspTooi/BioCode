package com.ksptool.bio.biz.core.model.root;

import com.ksptool.assembly.entity.exception.AuthException;
import com.ksptool.bio.biz.core.common.jpa.SnowflakeIdGenerated;
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

@Getter
@Setter
@Entity
@Table(name = "core_root")
@EntityListeners(AuditingEntityListener.class)
@SQLDelete(sql = "UPDATE core_root SET delete_time = NOW() WHERE id = ?")
@SQLRestriction("delete_time IS NULL")
public class CoreRootPo {

    @Id
    @SnowflakeIdGenerated
    @Column(name = "id", nullable = false, comment = "主键ID")
    private Long id;

    @Column(name = "name", nullable = false, length = 40, comment = "租户名称")
    private String name;

    @Column(name = "expire_time", comment = "到期时间(null长期)")
    private LocalDateTime expireTime;

    @Column(name = "remark", length = 200, comment = "备注")
    private String remark;

    @Column(name = "status", nullable = false, comment = "状态 0:正常 1:禁用")
    private Integer status;

    @Column(name = "admin_user_id", comment = "管理账号ID")
    private Long adminUserId;

    @Column(name = "is_system", columnDefinition = "TINYINT", nullable = false, comment = "内置租户 0:否 1:是")
    private Integer isSystem;

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

    @Column(name = "delete_time", comment = "删除时间")
    private LocalDateTime deleteTime;

    public boolean isSystem() {
        return isSystem != null && isSystem == 1;
    }

    /**
     * 判断租户是否已过期
     * @return 是否过期 true:过期 false:未过期
     */
    public boolean isExpired(){
        return expireTime != null && expireTime.isBefore(LocalDateTime.now());
    }       

    @PrePersist
    private void onCreate() throws AuthException {
    }

    @PreUpdate
    private void onUpdate() throws AuthException {

    }
}
