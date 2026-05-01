package com.ksptool.bio.biz.audit.model.auditerrorrcd;

import com.ksptool.bio.biz.core.common.jpa.SnowflakeIdGenerated;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "audit_error_rcd")
public class AuditErrorRcdPo {

    @Id
    @SnowflakeIdGenerated
    @Column(name = "id", comment = "错误ID")
    private Long id;

    @Column(name = "error_code", nullable = false, length = 32, comment = "错误代码")
    private String errorCode;

    @Column(name = "request_uri", nullable = false, comment = "请求地址")
    private String requestUri;

    @Column(name = "user_id", nullable = true, comment = "操作人ID")
    private Long userId;

    @Column(name = "user_name", nullable = true, comment = "操作人用户名")
    private String userName;

    @Column(name = "error_type", nullable = false, comment = "异常类型")
    private String errorType;

    @Column(name = "error_message", nullable = false, comment = "异常简述")
    private String errorMessage;

    @Column(name = "error_stack_trace", nullable = true, comment = "完整堆栈信息")
    private String errorStackTrace;

    @CreatedDate
    @Column(name = "create_time", nullable = false, comment = "创建时间")
    private LocalDateTime createTime;


    @PrePersist
    private void onCreate() {


    }

    @PreUpdate
    private void onUpdate() {


    }
}
