package com.ksptool.bio.biz.qf.model.qfbizform;

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
@Table(name = "qf_biz_form")
@EntityListeners({AuditingEntityListener.class, RsAuditingEntityListener.class})
@SQLDelete(sql = "UPDATE qf_biz_form SET delete_time = NOW() WHERE id = ?")
@SQLRestriction("delete_time IS NULL")
public class QfBizFormPo extends RowScopeRootOnlyPo {

    @Id
    @SnowflakeIdGenerated
    @Column(name = "id", nullable = false, comment = "主键ID")
    private Long id;

    @CreatedRootId
    @Column(name = "root_id", nullable = false, comment = "租户ID")
    private Long rootId;

    @Column(name = "name", nullable = false, length = 40, comment = "业务名称(如采购申请)")
    private String name;

    @Column(name = "code", nullable = false, length = 16, comment = "业务编码(如PURCHASE_APPLY, 必须全局唯一)")
    private String code;

    @Column(name = "form_type", nullable = false, columnDefinition = "tinyint", comment = "表单类型 0:手搓表单 1:动态表单")
    private Integer formType;

    @Column(name = "icon", nullable = false, length = 80, comment = "图标")
    private String icon;

    @Column(name = "table_name", nullable = false, length = 200, comment = "对应物理表名")
    private String tableName;

    @Column(name = "route_pc", length = 512, comment = "PC端路由名")
    private String routePc;

    @Column(name = "route_mobile", length = 512, comment = "移动端路由名")
    private String routeMobile;

    @Column(name = "status", nullable = false, columnDefinition = "tinyint", comment = "状态 0:正常 1:停用")
    private Integer status;

    @Column(name = "seq", nullable = false, comment = "排序")
    private Integer seq;

    @Column(name = "summary_template", length = 200, comment = "摘要模板")
    private String summaryTemplate;

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
