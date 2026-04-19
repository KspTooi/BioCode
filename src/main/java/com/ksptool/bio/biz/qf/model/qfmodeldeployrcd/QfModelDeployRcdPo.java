package com.ksptool.bio.biz.qf.model.qfmodeldeployrcd;

import com.ksptool.assembly.entity.exception.AuthException;
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
@Table(name = "qf_model_deploy_rcd")
@EntityListeners(AuditingEntityListener.class)
@SQLDelete(sql = "UPDATE qf_model_deploy_rcd SET delete_time = NOW() WHERE id = ?")
@SQLRestriction("delete_time IS NULL")
public class QfModelDeployRcdPo {

    @Id
    @SnowflakeIdGenerated
    @Column(name = "id", nullable = false, comment = "主键ID")
    private Long id;

    @Column(name = "root_id", nullable = false, comment = "所属企业/租户ID")
    private Long rootId;

    @Column(name = "dept_id", nullable = false, comment = "所属部门ID")
    private Long deptId;

    @Column(name = "model_id", nullable = false, comment = "模型ID")
    private Long modelId;

    @Column(name = "name", nullable = false, length = 80, comment = "模型名称")
    private String name;

    @Column(name = "code", nullable = false, length = 32, comment = "模型编码")
    private String code;

    @Column(name = "bpmn_xml", columnDefinition = "longtext", comment = "模型BPMN XML")
    private String bpmnXml;

    @Column(name = "version", nullable = false, comment = "模型版本号")
    private Integer version;

    @Column(name = "eng_deployment_id", length = 200, comment = "引擎部署ID(部署失败为NULL)")
    private String engDeploymentId;

    @Column(name = "eng_process_def_id", length = 200, comment = "引擎流程ID(部署失败为NULL)")
    private String engProcessDefId;

    @Column(name = "eng_deploy_result", nullable = false, length = 2000, comment = "引擎返回的部署结果")
    private String engDeployResult;

    @Column(name = "status", nullable = false, columnDefinition = "tinyint", comment = "部署状态 0:正常 1:部署失败")
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

    @Column(name = "delete_time", comment = "删除时间 NULL未删")
    private LocalDateTime deleteTime;


    @PrePersist
    private void onCreate() throws AuthException {
        var session = SessionService.session();
        if (this.rootId == null) {
            this.rootId = session.getRootId();
        }
        if (this.deptId == null) {
            this.deptId = session.getDeptId();
        }
    }

    @PreUpdate
    private void onUpdate() throws AuthException {

    }
}
