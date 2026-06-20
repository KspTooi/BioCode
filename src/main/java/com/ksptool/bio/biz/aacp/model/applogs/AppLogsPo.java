package com.ksptool.bio.biz.aacp.model.applogs;

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
@Table(name = "aacp_app_logs")
@EntityListeners(AuditingEntityListener.class)
@SQLDelete(sql = "UPDATE aacp_app_logs SET delete_time = NOW() WHERE id = ?")
@SQLRestriction("delete_time IS NULL")
public class AppLogsPo {

    @Id
    @SnowflakeIdGenerated
    @Column(name = "id", nullable = false, comment = "主键ID")
    private Long id;

    @Column(name = "root_id", nullable = false, comment = "租户ID")
    private Long rootId;

    @Column(name = "app_id", nullable = false, comment = "应用ID")
    private Long appId;

    @Column(name = "provider_id", nullable = false, comment = "供应商ID")
    private Long providerId;

    @Column(name = "model_id", nullable = false, comment = "模型变体ID")
    private Long modelId;

    @Column(name = "input_token", comment = "输入词元")
    private Integer inputToken;

    @Column(name = "output_token", comment = "输出词元")
    private Integer outputToken;

    @Column(name = "cost", comment = "消耗金额")
    private String cost;

    @Column(name = "start_time", nullable = false, comment = "发起时间")
    private LocalDateTime startTime;

    @Column(name = "end_time", comment = "结束时间")
    private LocalDateTime endTime;

    @Column(name = "duration_ms", comment = "总耗时MS")
    private Integer durationMs;

    @Column(name = "ttfb_ms", comment = "首字响应时间")
    private Integer ttfbMs;

    @Column(name = "status_code", length = 40, comment = "HTTP状态码")
    private String statusCode;

    @Column(name = "client_ip", nullable = false, length = 45, comment = "客户端IP")
    private String clientIp;

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
