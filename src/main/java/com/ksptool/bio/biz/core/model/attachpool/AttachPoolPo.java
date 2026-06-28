package com.ksptool.bio.biz.core.model.attachpool;

import com.ksptool.assembly.entity.exception.AuthException;
import com.ksptool.bio.biz.auth.common.aop.RowScopeRootOnlyPo;
import com.ksptool.bio.biz.auth.common.aop.CreatedRootId;
import com.ksptool.bio.biz.auth.common.aop.RsAuditingEntityListener;
import com.ksptool.bio.biz.auth.service.SessionService;
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

@Getter
@Setter
@Entity
@Table(name = "core_attach_pool")
@EntityListeners({AuditingEntityListener.class, RsAuditingEntityListener.class})
public class AttachPoolPo extends RowScopeRootOnlyPo {

    @Id
    @SnowflakeIdGenerated
    @Column(name = "id", nullable = false, comment = "主键ID")
    private Long id;

    @CreatedRootId
    @Column(name = "root_id", nullable = false, comment = "租户ID")
    private Long rootId;

    @Column(name = "pool_path", nullable = false, length = 2048, comment = "存储池地址")
    private String poolPath;

    @Column(name = "pool_capacity_bytes", nullable = false, comment = "总可用字节")
    private Long poolCapacityBytes;

    @Column(name = "pool_attaches_bytes", comment = "附件占用字节")
    private Long poolAttachesBytes;

    @Column(name = "indexed_count", nullable = false, comment = "已索引附件数")
    private Integer indexedCount;

    @Column(name = "drift_count", nullable = false, comment = "游离附件数")
    private Integer driftCount;

    @Column(name = "scan_start_time", nullable = false, comment = "扫描开始时间")
    private LocalDateTime scanStartTime;

    @Column(name = "scan_end_time", comment = "扫描结束时间")
    private LocalDateTime scanEndTime;

    @Column(name = "scan_status", nullable = false,columnDefinition = "tinyint", comment = "扫描状态 0:正在扫描 1:成功")
    private Integer scanStatus;

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
