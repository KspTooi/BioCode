package com.ksptool.bio.biz.assembly.model.polymodel;

import com.ksptool.assembly.entity.exception.AuthException;
import com.ksptool.bio.biz.core.common.jpa.SetStringConv;
import com.ksptool.bio.biz.core.common.jpa.SnowflakeIdGenerated;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * @author KspTooi
 * @since 1.6.19(S).32
 */
@Getter
@Setter
@Entity
@Table(name = "assembly_poly_model")
@EntityListeners(AuditingEntityListener.class)
public class PolyModelPo {

    @Id
    @SnowflakeIdGenerated
    @Column(name = "id", nullable = false, comment = "主键ID")
    private Long id;

    @Column(name = "output_schema_id", nullable = false, comment = "输出方案ID")
    private Long outputSchemaId;

    @Column(name = "name", nullable = false, length = 255, comment = "字段名")
    private String name;

    @Column(name = "data_type", nullable = false, length = 255, comment = "数据类型")
    private String dataType;

    @Column(name = "length", length = 255, comment = "长度")
    private Integer length;

    @Column(name = "`require`", nullable = false, columnDefinition = "TINYINT", comment = "必填 0:否 1:是")
    private Integer require;

    @Column(name = "policy_crud_json", nullable = false, columnDefinition = "JSON", comment = "可见性策略 ADD、EDIT、DETAILS、LIST_QUERY、LIST_VIEW")
    @Convert(converter = SetStringConv.class)
    private Set<String> policyCrudJson;

    @Column(name = "policy_query", nullable = false, columnDefinition = "TINYINT", comment = "查询策略 0:等于")
    private Integer policyQuery;

    @Column(name = "policy_view", nullable = false, columnDefinition = "TINYINT", comment = "显示策略 0:文本框 1:文本域 2:下拉 3:单 4:多 5:LD 6:LDT")
    private Integer policyView;

    @Column(name = "pk", nullable = false, columnDefinition = "TINYINT", comment = "是否主键 0:否 1:是")
    private Integer pk;

    @Column(name = "remark", nullable = false, length = 80, comment = "字段备注")
    private String remark;

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

}
