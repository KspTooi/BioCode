package com.ksptool.bio.biz.core.model.pack;

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
@Table(name = "core_root_pack")
@EntityListeners(AuditingEntityListener.class)
public class RootPackPo {

    @Id
    @SnowflakeIdGenerated
    @Column(name = "root_id", nullable = false, comment = "租户ID")
    private Long rootId;

    @Id
    @SnowflakeIdGenerated
    @Column(name = "pack_id", nullable = false, comment = "菜单包ID")
    private Long packId;


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
