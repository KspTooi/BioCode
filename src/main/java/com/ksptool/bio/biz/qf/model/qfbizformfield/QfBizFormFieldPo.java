package com.ksptool.bio.biz.qf.model.qfbizformfield;

import com.ksptool.assembly.entity.exception.AuthException;
import com.ksptool.bio.biz.auth.common.aop.*;
import com.ksptool.bio.biz.core.common.jpa.SnowflakeIdGenerated;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Getter
@Setter
@Entity
@Table(name = "qf_biz_form_field")
@EntityListeners({AuditingEntityListener.class, RsAuditingEntityListener.class})
@SQLDelete(sql = "UPDATE qf_biz_form_field SET delete_time = NOW() WHERE id = ?")
@SQLRestriction("delete_time IS NULL")
public class QfBizFormFieldPo extends RowScopeRootOnlyPo {

    @Id
    @SnowflakeIdGenerated
    @Column(name = "id", nullable = false, comment = "主键ID")
    private Long id;
    
    @CreatedRootId
    @Column(name = "root_id", nullable = false, comment = "租户ID")
    private Long rootId;

    @Column(name = "form_id", nullable = false, comment = "业务表ID")
    private Long formId;

    @Column(name = "field_name", nullable = false, length = 32, comment = "字段名")
    private String fieldName;

    @Column(name = "remark", nullable = false, length = 32, comment = "备注")
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

    @Column(name = "delete_time", comment = "删除时间")
    private LocalDateTime deleteTime;


    @PrePersist
    private void onCreate() throws AuthException {
    }

    @PreUpdate
    private void onUpdate() throws AuthException {

    }
}
