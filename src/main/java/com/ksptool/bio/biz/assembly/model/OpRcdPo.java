package com.ksptool.bio.biz.assembly.model;

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
@Table(name = "assembly_op_rcd")
@EntityListeners(AuditingEntityListener.class)
@SQLDelete(sql = "UPDATE assembly_op_rcd SET delete_time = NOW() WHERE id = ?")
@SQLRestriction("delete_time IS NULL")
public class OpRcdPo {

    @Id
    @SnowflakeIdGenerated
    @Column(name = "id", nullable = false, comment = "主键ID")
    private Long id;

    @Column(name = "op_name", nullable = false, length = 32, comment = "输出方案名称")
    private String opName;

    @Column(name = "ds_name", nullable = false, length = 32, comment = "数据源名称")
    private String dsName;

    @Column(name = "ds_table_name", nullable = false, length = 80, comment = "数据源表名")
    private String dsTableName;

    @Column(name = "ds_url", nullable = false, comment = "数据源连接字符串")
    private String dsUrl;

    @Column(name = "scm_input_url", nullable = false, length = 2048, comment = "输入SCM仓库地址")
    private String scmInputUrl;

    @Column(name = "scm_output_url", nullable = false, length = 2048, comment = "输出SCM仓库地址")
    private String scmOutputUrl;

    @Column(name = "model_name", nullable = false, length = 255, comment = "模型名称")
    private String modelName;

    @Column(name = "model_remark", nullable = false, length = 80, comment = "模型备注")
    private String modelRemark;

    @Column(name = "biz_domain", nullable = false, length = 80, comment = "业务域")
    private String bizDomain;

    @Column(name = "qbe_params", nullable = false, comment = "QBE参数")
    private String qbeParams;

    @Column(name = "start_time", nullable = false, comment = "开始时间")
    private LocalDateTime startTime;

    @Column(name = "end_time", nullable = false, comment = "结束时间")
    private LocalDateTime endTime;

    @Column(name = "duration_ms", nullable = false, comment = "耗时MS")
    private Integer durationMs;

    @Column(name = "creator_username", nullable = false, length = 80, comment = "操作人账号")
    private String creatorUsername;

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
