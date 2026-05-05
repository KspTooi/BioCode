package com.ksptool.bio.biz.document.model.prompt;

import com.ksptool.assembly.entity.exception.AuthException;
import com.ksptool.bio.biz.auth.common.aop.RowScopeRootOnlyPo;
import com.ksptool.bio.biz.auth.service.SessionService;
import com.ksptool.bio.biz.core.common.jpa.ListCTJConv;
import com.ksptool.bio.biz.core.common.jpa.SnowflakeIdGenerated;
import com.ksptool.bio.biz.core.common.model.CustomizeTagJson;
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
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "ep_prompt")
@EntityListeners(AuditingEntityListener.class)
@SQLDelete(sql = "UPDATE ep_prompt SET delete_time = NOW() WHERE id = ?")
@SQLRestriction("delete_time IS NULL")
public class PromptPo extends RowScopeRootOnlyPo {

    @Id
    @SnowflakeIdGenerated
    @Column(name = "id", nullable = false, comment = "主键ID")
    private Long id;

    @Column(name = "root_id", nullable = false, comment = "租户ID")
    private Long rootId;

    @Column(name = "dept_id", nullable = false, comment = "部门ID")
    private Long deptId;

    @Column(name = "name", nullable = false, length = 80, comment = "名称")
    private String name;

    @Column(name = "tags", nullable = false, columnDefinition = "JSON", comment = "标签")
    @Convert(converter = ListCTJConv.class)
    private List<CustomizeTagJson> tags;

    @Column(name = "content", comment = "内容")
    private String content;

    @Column(name = "param_count", nullable = false, comment = "参数数量")
    private Integer paramCount;

    @Column(name = "version", nullable = false, comment = "版本号")
    private Integer version;

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

        //自动填充租户ID和部门ID
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
