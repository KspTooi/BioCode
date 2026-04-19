package com.ksptool.bio.biz.qfcc.model;

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
@Table(name = "qf_cc")
@EntityListeners(AuditingEntityListener.class)
@SQLDelete(sql = "UPDATE qf_cc SET delete_time = NOW() WHERE id = ?")
@SQLRestriction("delete_time IS NULL")
public class QfCcPo {

    @Id
    @SnowflakeIdGenerated
    @Column(name = "id", nullable = false, comment = "主键ID")
    private Long id;

    @Column(name = "root_id", nullable = false, comment = "租户ID")
    private Long rootId;

    @Column(name = "dept_id", nullable = false, comment = "部门ID")
    private Long deptId;

    @Column(name = "eng_proc_id", nullable = false, length = 200, comment = "引擎流程ID")
    private String engProcId;

    @Column(name = "biz_form_id", nullable = false, comment = "业务表单ID")
    private Long bizFormId;

    @Column(name = "table_name", nullable = false, length = 200, comment = "物理表名(带入业务表单数据)")
    private String tableName;

    @Column(name = "data_id", nullable = false, comment = "物理表数据主键ID")
    private Long dataId;

    @Column(name = "node_name", nullable = false, length = 80, comment = "当前节点名称 (如: 财务总监审批)")
    private String nodeName;

    @Column(name = "summary", nullable = false, length = 500, comment = "摘要(如：张三提交的 5000 元报销)")
    private String summary;

    @Column(name = "from_id", comment = "抄送发起人ID(自动抄送为null)")
    private Long fromId;

    @Column(name = "from_name", length = 20, comment = "抄送发起人姓名")
    private String fromName;

    @Column(name = "target_id", nullable = false, comment = "被抄送人ID")
    private Long targetId;

    @Column(name = "is_read", nullable = false, comment = "是否读 0:未读 1:已读")
    private Integer isRead;

    @Column(name = "read_time", comment = "读取时间")
    private LocalDateTime readTime;

    @CreatedDate
    @Column(name = "create_time", nullable = false, comment = "抄送时间")
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
