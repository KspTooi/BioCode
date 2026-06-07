package com.ksptool.bio.biz.qf.model.qfmodel;

import com.ksptool.assembly.entity.exception.AuthException;
import com.ksptool.bio.biz.auth.common.aop.*;
import com.ksptool.bio.biz.auth.service.SessionService;
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
@Table(name = "qf_model")
@EntityListeners({AuditingEntityListener.class, RsAuditingEntityListener.class})
@SQLDelete(sql = "UPDATE qf_model SET delete_time = NOW() WHERE id = ?")
@SQLRestriction("delete_time IS NULL")
public class QfModelPo extends RowScopeRootOnlyPo {

    @Id
    @SnowflakeIdGenerated
    @Column(name = "id", nullable = false, comment = "主键ID")
    private Long id;

    @CreatedRootId
    @Column(name = "root_id", nullable = false, comment = "所属企业/租户ID")
    private Long rootId;


    @Column(name = "active_deploy_id", comment = "该模型生效的部署ID")
    private Long activeDeployId;

    @Column(name = "group_id", comment = "模型组ID")
    private Long groupId;

    @Column(name = "form_id", comment = "关联表单ID")
    private Long formId;

    @Column(name = "name", nullable = false, length = 80, comment = "模型名称")
    private String name;

    @Column(name = "code", nullable = false, length = 32, comment = "模型编码")
    private String code;

    @Column(name = "bpmn_xml", columnDefinition = "longtext", comment = "模型BPMN XML")
    private String bpmnXml;

    @Column(name = "version", nullable = false, comment = "模型版本号")
    private Integer version;

    @Column(name = "status", nullable = false, columnDefinition = "tinyint", comment = "模型状态 0:草稿 1:已部署 2:历史")
    private Integer status;

    @Column(name = "seq", nullable = false, comment = "排序")
    private Integer seq;

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

    @Column(name = "delete_time", comment = "删除时间 NULL未删")
    private LocalDateTime deleteTime;


    @PrePersist
    private void onCreate() throws AuthException {

    }

    @PreUpdate
    private void onUpdate() throws AuthException {

    }
}
