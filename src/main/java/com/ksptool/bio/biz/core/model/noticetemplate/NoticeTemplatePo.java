package com.ksptool.bio.biz.core.model.noticetemplate;

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
@EntityListeners(AuditingEntityListener.class)
@Table(name = "core_notice_template")
@SQLDelete(sql = "UPDATE core_notice_template SET delete_time = NOW() WHERE id = ?")
@SQLRestriction("delete_time IS NULL")
public class NoticeTemplatePo {

    @Id
    @SnowflakeIdGenerated
    @Column(name = "id", nullable = false, comment = "主键ID")
    private Long id;

    @Column(name = "name", length = 32, nullable = false, comment = "模板名称")
    private String name;

    @Column(name = "code", length = 32, nullable = false, comment = "模板唯一编码 (业务调用用)")
    private String code;

    @Column(name = "content", columnDefinition = "LONGTEXT", nullable = false, comment = "模板内容 (含占位符)")
    private String content;

    @Column(name = "status", columnDefinition = "TINYINT", nullable = false, comment = "状态: 0启用, 1禁用")
    private Integer status;

    @Column(name = "remark", length = 1000, nullable = true, comment = "备注")
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

    @Column(name = "delete_time", nullable = true, comment = "删除时间 NULL未删")
    private LocalDateTime deleteTime;


    @PrePersist
    private void onCreate() throws AuthException {

    }

    @PreUpdate
    private void onUpdate() throws AuthException {


    }
}
