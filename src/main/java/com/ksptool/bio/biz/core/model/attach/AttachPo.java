package com.ksptool.bio.biz.core.model.attach;

import com.ksptool.assembly.entity.exception.AuthException;
import com.ksptool.bio.biz.auth.service.SessionService;
import com.ksptool.bio.biz.core.common.jpa.SnowflakeIdGenerated;
import com.ksptool.bio.biz.auth.common.aop.CreatedRootId;
import com.ksptool.bio.biz.auth.common.aop.RsAuditingEntityListener;
import com.ksptool.bio.commons.utils.IdWorker;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.BatchSize;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@EntityListeners({AuditingEntityListener.class, RsAuditingEntityListener.class})
@Table(name = "core_attach")
public class AttachPo {

    @Id
    @SnowflakeIdGenerated
    @Column(name = "id", comment = "主键ID")
    private Long id;

    @CreatedRootId
    @Column(name = "root_id", nullable = false, comment = "租户ID")
    private Long rootId;

    @Column(name = "name", length = 80, nullable = false, comment = "文件原始名称")
    private String name;

    @Column(name = "kind", length = 32, nullable = false, comment = "文件业务类型")
    private String kind;

    @Column(name = "suffix", length = 80, comment = "扩展名")
    private String suffix;

    @Column(name = "path", length = 2048, nullable = false, comment = "文件路径")
    private String path;

    @Column(name = "sha256", length = 64, nullable = false, comment = "文件摘要")
    private String sha256;

    @Column(name = "total_size", nullable = false, comment = "文件总大小")
    private Long totalSize;

    @Column(name = "receive_size", nullable = false, comment = "已接收大小")
    private Long receiveSize;

    @Column(name = "status", columnDefinition = "tinyint", nullable = false, comment = "状态 0:预检文件 1:区块不完整 2:校验中 3:有效")
    private Integer status;

    @Column(name = "verify_time", comment = "校验时间")
    private LocalDateTime verifyTime;

    @CreatedDate
    @Column(name = "create_time", nullable = false, comment = "创建时间")
    private LocalDateTime createTime;

    @CreatedBy
    @Column(name = "creator_id", nullable = false, comment = "创建人ID")
    private Long creatorId;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "attach", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("chunkId ASC")
    @BatchSize(size = 100)
    private List<AttachChunkPo> chunks;

    public void addChunk(long chunkId) {

        if (chunks == null) {
            chunks = new ArrayList<>();
        }

        var chunk = new AttachChunkPo();
        chunk.setId(IdWorker.nextId());
        chunk.setAttach(this);
        chunk.setChunkId(chunkId);
        chunk.setCreateTime(LocalDateTime.now());
        chunks.add(chunk);
    }

    @PrePersist
    private void onCreate() throws AuthException {


    }


}
