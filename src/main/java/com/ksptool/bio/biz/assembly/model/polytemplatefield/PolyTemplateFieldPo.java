package com.ksptool.bio.biz.assembly.model.polytemplatefield;

import com.ksptool.assembly.entity.exception.AuthException;
import com.ksptool.bio.biz.core.common.jpa.SetStringConv;
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
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "assembly_poly_template_field")
@EntityListeners(AuditingEntityListener.class)
@SQLDelete(sql = "UPDATE assembly_poly_template_field SET delete_time = NOW() WHERE id = ?")
@SQLRestriction("delete_time IS NULL")
public class PolyTemplateFieldPo {

    @Id
    @SnowflakeIdGenerated
    @Column(name = "id", nullable = false, comment = "主键ID")
    private Long id;

    @Column(name = "poly_template_id", nullable = false, comment = "聚合模板ID")
    private Long polyTemplateId;

    @Column(name = "name", nullable = false, length = 255, comment = "字段名")
    private String name;

    @Column(name = "policy_crud_json", nullable = false, columnDefinition = "JSON", comment = "可见性策略 ADD、EDIT、DETAILS、LIST_QUERY、LIST_VIEW")
    @Convert(converter = SetStringConv.class)
    private Set<String> policyCrudJson;

    @Column(name = "policy_query", nullable = false, columnDefinition = "TINYINT", comment = "查询策略 0:等于")
    private Integer policyQuery;

    @Column(name = "policy_view", nullable = false, columnDefinition = "TINYINT", comment = "显示策略 0:文本框 1:文本域 2:下拉 3:单 4:多 5:LD 6:LDT")
    private Integer policyView;

    @Column(name = "seq", nullable = false, columnDefinition = "TINYINT", comment = "排序")
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

    @Column(name = "delete_time", comment = "删除时间")
    private LocalDateTime deleteTime;

}
