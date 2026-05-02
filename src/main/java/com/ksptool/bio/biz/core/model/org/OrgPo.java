package com.ksptool.bio.biz.core.model.org;


import com.ksptool.assembly.entity.exception.AuthException;
import com.ksptool.bio.biz.auth.common.aop.RowScopePo;
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
@EntityListeners(AuditingEntityListener.class)
@Table(name = "core_org")
@SQLRestriction(value = "delete_time IS NULL")
@SQLDelete(sql = "UPDATE core_org SET delete_time = NOW() WHERE id = ?")
public class OrgPo extends RowScopePo {

    @Id
    @SnowflakeIdGenerated
    @Column(name = "id", comment = "主键id")
    private Long id;

    @Column(name = "root_id", nullable = false, comment = "租户ID")
    private Long rootId;

    @Column(name = "top_id", nullable = false, comment = "顶级企业ID")
    private Long topId;

    @Column(name = "org_id", nullable = false, comment = "直属企业ID")
    private Long orgId;

    @Column(name = "parent_id", comment = "上级组织ID NULL顶级组织")
    private Long parentId;

    @Column(name = "org_path_ids",nullable = false, length = 1024, comment = "从顶级组织到当前组织的ID列表 以,分割")
    private String orgPathIds;

    @Column(name = "kind", nullable = false, columnDefinition = "tinyint", comment = "0:企业 1:子企业 2:部门")
    private Integer kind;

    @Column(name = "name", nullable = false, length = 80, comment = "组织机构名称")
    private String name;

    @Column(name = "short_name", length = 40, comment = "组织机构简称")
    private String shortName;

    @Column(name = "level", nullable = false, columnDefinition = "tinyint", comment = "组织机构级别")
    private Integer level;

    @Column(name = "principal_id", comment = "主管ID")
    private Long principalId;

    @Column(name = "seq", nullable = false, comment = "排序")
    private Integer seq;

    @Column(name = "remark", length = 200, comment = "备注")
    private String remark;

    @CreatedDate
    @Column(name = "create_time", nullable = false, comment = "创建时间")
    private LocalDateTime createTime;

    @CreatedBy
    @Column(name = "creator_id", nullable = false, comment = "创建人id")
    private Long creatorId;

    @LastModifiedDate
    @Column(name = "update_time", nullable = false, comment = "更新时间")
    private LocalDateTime updateTime;

    @LastModifiedBy
    @Column(name = "updater_id", nullable = false, comment = "更新人id")
    private Long updaterId;

    @Column(name = "delete_time", comment = "删除时间 NULL未删除")
    private LocalDateTime deleteTime;

    @PrePersist
    private void onCreate() throws AuthException {
        if(rootId == null){
            rootId = SessionService.session().getRootId();
        }
    }

    @PreUpdate
    private void onUpdate() throws AuthException {

    }

    /**
     * 是否为公司
     * @return true:是公司 false:不是公司
     */
    public boolean isCompany() {
        return this.kind == 0;
    }

    /**
     * 是否为子公司
     * @return true:是子公司 false:不是子公司
     */
    public boolean isSubCompany() {
        return this.kind == 1;
    }

    /**
     * 是否为部门
     * @return true:是部门 false:不是部门
     */
    public boolean isDepartment() {
        return this.kind == 2;
    }
    
}
