package com.ksptool.bio.biz.aacp.model.model;

import com.ksptool.bio.biz.auth.common.aop.CreatedRootId;
import com.ksptool.bio.biz.auth.common.aop.RsAuditingEntityListener;
import com.ksptool.bio.biz.auth.common.aop.RowScopeRootOnlyPo;
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
@Table(name = "aacp_model")
@EntityListeners({AuditingEntityListener.class, RsAuditingEntityListener.class})
@SQLDelete(sql = "UPDATE aacp_model SET delete_time = NOW() WHERE id = ?")
@SQLRestriction("delete_time IS NULL")
public class AacpModelPo extends RowScopeRootOnlyPo {

    @Id
    @SnowflakeIdGenerated
    @Column(name = "id", nullable = false, comment = "主键ID")
    private Long id;

    @CreatedRootId
    @Column(name = "root_id", nullable = false, comment = "租户ID")
    private Long rootId;

    @Column(name = "name", nullable = false, length = 80, comment = "模型变体名称")
    private String name;

    @Column(name = "code", nullable = false, length = 64, comment = "模型标识")
    private String code;

    @Column(name = "kind", nullable = false, columnDefinition = "TINYINT", comment = "类型 0:文本 1:图形 2:音频 3:多模态")
    private Integer kind;

    @Column(name = "max_context", nullable = false, comment = "最大上下文长度")
    private Integer maxContext;

    @Column(name = "max_output_token", nullable = false, comment = "最大输出词元")
    private Integer maxOutputToken;

    @Column(name = "api_reasoning", nullable = false, columnDefinition = "TINYINT", comment = "推理 0:不支持 1:支持")
    private Integer apiReasoning;

    @Column(name = "api_reasoning_effort", nullable = false, columnDefinition = "TINYINT", comment = "推理强度 0:关 1:低 2:中 3:高 4:极高")
    private Integer apiReasoningEffort;

    @Column(name = "api_append_param", nullable = false, columnDefinition = "JSON", comment = "附加参数")
    private String apiAppendParam;

    @Column(name = "api_append_headers", nullable = false, columnDefinition = "JSON", comment = "附加请求头")
    private String apiAppendHeaders;

    @Column(name = "finc_input", nullable = false, comment = "输入单价")
    private String fincInput;

    @Column(name = "finc_input_cached", nullable = false, comment = "输入单价(缓存)")
    private String fincInputCached;

    @Column(name = "finc_output", nullable = false, comment = "输出单价")
    private String fincOutput;

    @Column(name = "test_ttfb", comment = "测试首字响应时间 MS")
    private Integer testTtfb;

    @Column(name = "test_rate", comment = "测试响应速率 T/S")
    private Integer testRate;

    @Column(name = "test_time", comment = "最后测试时间")
    private LocalDateTime testTime;

    @Column(name = "remark", length = 200, comment = "备注")
    private String remark;

    @Column(name = "seq", nullable = false, columnDefinition = "TINYINT", comment = "排序")
    private Integer seq;

    @Column(name = "status", nullable = false, columnDefinition = "TINYINT", comment = "状态 0:禁用 1:启用")
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

}