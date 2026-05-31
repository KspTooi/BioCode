package com.ksptool.bio.biz.qf.model.qftodo;

import com.ksptool.assembly.entity.exception.AuthException;
import com.ksptool.bio.biz.auth.service.SessionService;
import com.ksptool.bio.biz.core.common.jpa.SnowflakeIdGenerated;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "qf_todo")
@EntityListeners(AuditingEntityListener.class)
@SQLDelete(sql = "UPDATE qf_todo SET delete_time = NOW() WHERE id = ?")
@SQLRestriction("delete_time IS NULL")
public class QfTodoPo {

    @Id
    @SnowflakeIdGenerated
    @Column(name = "id", nullable = false, comment = "主键ID")
    private Long id;

    @Column(name = "root_id", nullable = false, comment = "租户ID")
    private Long rootId;

    @Column(name = "dept_id", nullable = false, comment = "部门ID")
    private Long deptId;

    @Column(name = "eng_task_id", nullable = false, length = 200, comment = "引擎任务ID")
    private String engTaskId;

    @Column(name = "eng_proc_id", nullable = false, length = 200, comment = "引擎流程ID")
    private String engProcId;

    @Column(name = "biz_form_id", nullable = false, comment = "业务表单ID")
    private Long bizFormId;

    @Column(name = "eng_process_def_id", length = 200, comment = "引擎流程ID(部署失败为NULL)")
    private String engProcessDefId;

    @Column(name = "table_name", nullable = false, length = 200, comment = "物理表名(带入业务表单数据)")
    private String tableName;

    @Column(name = "data_id", nullable = false, comment = "物理表数据主键ID")
    private Long dataId;

    @Column(name = "node_name", nullable = false, length = 80, comment = "当前节点名称 (如: 财务总监审批)")
    private String nodeName;

    @Column(name = "summary", nullable = false, length = 500, comment = "摘要(如：张三提交的 5000 元报销)")
    private String summary;

    @Column(name = "member_type", nullable = false, comment = "办理成员类型 0:办理人, 1:候选组")
    private Integer memberType;

    @Column(name = "member_id", nullable = false, comment = "办理成员ID (用户ID或用户组标识)")
    private Long memberId;

    @Column(name = "initiator_id", nullable = false, comment = "发起人ID")
    private Long initiatorId;

    @Column(name = "initiator_name", nullable = false, length = 20, comment = "发起人名")
    private String initiatorName;

    @Column(name = "initiator_time", nullable = false, comment = "发起时间")
    private LocalDateTime initiatorTime;

    @Column(name = "status", nullable = false, columnDefinition = "TINYINT", comment = "待办状态 0:待办 1:已办 10:已作废")
    private Integer status;

    @Column(name = "fin_member_id", comment = "实际办理人ID")
    private Long finMemberId;

    @Column(name = "fin_member_name", length = 20, comment = "实际办理人姓名")
    private String finMemberName;

    @Column(name = "fin_time", comment = "实际办理时间")
    private LocalDateTime finTime;

    @Column(name = "action", columnDefinition = "TINYINT", comment = "操作 0:同意 1:驳回")
    private Integer action;

    @Column(name = "comment", length = 500, comment = "办理人意见")
    private String comment;

    @Column(name = "duration", comment = "耗时(毫秒)")
    private Long duration;

    @CreatedDate
    @Column(name = "create_time", nullable = false, comment = "任务到达时间")
    private LocalDateTime createTime;

    @Column(name = "delete_time", comment = "删除时间")
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
