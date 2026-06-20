package com.ksptool.bio.biz.polytemplate.model;

import com.ksptool.assembly.entity.exception.AuthException;
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
import com.ksptool.assembly.entity.exception.AuthException;
import com.ksptool.bio.biz.auth.service.SessionService;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Getter
@Setter
@Entity
@Table(name = "assembly_poly_template")
@EntityListeners(AuditingEntityListener.class)
@SQLDelete(sql = "UPDATE assembly_poly_template SET delete_time = NOW() WHERE id = ?")
@SQLRestriction("delete_time IS NULL")
public class PolyTemplatePo {

    @Column(name = "id", nullable = false, comment = "主键ID")
    private Long id;

    @Column(name = "name", nullable = false, length = 40, comment = "模板名称")
    private String name;

    @Column(name = "code", nullable = false, length = 16, comment = "模板代码")
    private String code;

    @Column(name = "seq", nullable = false, comment = "排序")
    private Integer seq;

    @Column(name = "status", nullable = false, comment = "状态 0:禁用 1:启用")
    private Integer status;

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
