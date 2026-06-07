package com.ksptool.bio.biz.mcpcapablity.model;

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
@Table(name = "aacp_mcp_capablity")
@EntityListeners(AuditingEntityListener.class)
public class McpCapablityPo {

    @Id
    @SnowflakeIdGenerated
    @Column(name = "mcp_id", nullable = false, comment = "MID")
    private Long mcpId;

    @Id
    @SnowflakeIdGenerated
    @Column(name = "capability_id", nullable = false, comment = "CID")
    private Long capabilityId;


    @PrePersist
    private void onCreate() throws AuthException {
    }

    @PreUpdate
    private void onUpdate() throws AuthException {

    }
}
