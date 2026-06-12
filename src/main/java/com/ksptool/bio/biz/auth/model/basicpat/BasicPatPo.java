package com.ksptool.bio.biz.auth.model.basicpat;

import com.ksptool.assembly.entity.exception.AuthException;
import com.ksptool.bio.biz.auth.common.aop.CreatedRootId;
import com.ksptool.bio.biz.auth.common.aop.RowScopeRootOnlyPo;
import com.ksptool.bio.biz.auth.common.aop.RsAuditingEntityListener;
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
@Table(name = "auth_basic_pat", comment = "基本PAT表")
@EntityListeners({AuditingEntityListener.class, RsAuditingEntityListener.class})
@SQLDelete(sql = "UPDATE auth_basic_pat SET delete_time = NOW() WHERE id = ?")
@SQLRestriction("delete_time IS NULL")
public class BasicPatPo extends RowScopeRootOnlyPo {

    @Id
    @SnowflakeIdGenerated
    @Column(name = "id", nullable = false, comment = "主键ID")
    private Long id;

    @CreatedRootId
    @Column(name = "root_id", nullable = false, comment = "租户ID")
    private Long rootId;

    @Column(name = "user_id", nullable = false, comment = "所属用户ID")
    private Long userId;

    @Column(name = "name", nullable = false, length = 40, comment = "PAT名称")
    private String name;

    @Column(name = "pat_pt", nullable = false, length = 200, comment = "部分明文")
    private String patPt;

    @Column(name = "pat_ct", nullable = false, length = 2048, comment = "密文")
    private String patCt;

    @Column(name = "expire", comment = "过期时间")
    private LocalDateTime expire;

    @Column(name = "status", nullable = false, columnDefinition = "TINYINT", comment = "状态: 0:禁用 1:启用")
    private Integer status = 1;

    @CreatedDate
    @Column(name = "create_time", nullable = false, updatable = false, comment = "创建时间")
    private LocalDateTime createTime;

    @CreatedBy
    @Column(name = "creator_id", nullable = false, updatable = false, comment = "创建人ID")
    private Long creatorId;

    @LastModifiedDate
    @Column(name = "update_time", nullable = false, comment = "更新时间")
    private LocalDateTime updateTime;

    @LastModifiedBy
    @Column(name = "updater_id", nullable = false, comment = "更新人ID")
    private Long updaterId;

    @Column(name = "delete_time", comment = "删除时间")
    private LocalDateTime deleteTime;

    @PrePersist
    private void onCreate() throws AuthException {
    }
}
