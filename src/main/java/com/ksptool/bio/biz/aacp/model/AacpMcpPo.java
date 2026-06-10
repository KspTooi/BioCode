package com.ksptool.bio.biz.aacp.model;

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
@Table(name = "aacp_mcp")
@EntityListeners(AuditingEntityListener.class)
@SQLDelete(sql = "UPDATE aacp_mcp SET delete_time = NOW() WHERE id = ?")
@SQLRestriction("delete_time IS NULL")
public class AacpMcpPo {

    @Id
    @SnowflakeIdGenerated
    @Column(name = "id", nullable = false, comment = "主键ID")
    private Long id;

    @Column(name = "root_id", nullable = false, comment = "租户ID")
    private Long rootId;

    @Column(name = "name", nullable = false, length = 40, comment = "服务器名称")
    private String name;

    @Column(name = "code", nullable = false, length = 16, comment = "唯一编码")
    private String code;

    @Column(name = "network_kind", nullable = false, columnDefinition = "tinyint", comment = "通信协议 0:HTTP+SSE 1:WS")
    private Integer networkKind;

    @Column(name = "auth_kind", nullable = false, columnDefinition = "tinyint", comment = "鉴权类型 0:无 1:PSK")
    private Integer authKind;

    @Column(name = "auth_psk", length = 2000, comment = "预共享密钥")
    private String authPsk;

    @Column(name = "status", nullable = false, columnDefinition = "tinyint", comment = "状态 0:离线 1:在线")
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


    @PrePersist
    private void onCreate() throws AuthException {
        var session = SessionService.session();
        if (this.rootId == null) {
            this.rootId = session.getRootId();
        }
    }

    @PreUpdate
    private void onUpdate() throws AuthException {

    }
}
