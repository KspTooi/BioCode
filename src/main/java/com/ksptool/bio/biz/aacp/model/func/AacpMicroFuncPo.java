package com.ksptool.bio.biz.aacp.model.func;

import com.ksptool.assembly.entity.exception.AuthException;
import com.ksptool.bio.biz.auth.common.aop.CreatedRootId;
import com.ksptool.bio.biz.auth.common.aop.RsAuditingEntityListener;
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
@Table(name = "aacp_micro_func")
@EntityListeners({AuditingEntityListener.class, RsAuditingEntityListener.class})
@SQLDelete(sql = "UPDATE aacp_micro_func SET delete_time = NOW() WHERE id = ?")
@SQLRestriction("delete_time IS NULL")
public class AacpMicroFuncPo {

    @Id
    @SnowflakeIdGenerated
    @Column(name = "id", nullable = false, comment = "主键ID")
    private Long id;

    @CreatedRootId
    @Column(name = "root_id", nullable = false, comment = "租户ID")
    private Long rootId;

    @Column(name = "name", nullable = false, length = 40, comment = "微函数名称")
    private String name;

    @Column(name = "code", nullable = false, length = 32, comment = "微函数标识")
    private String code;

    @Column(name = "description", nullable = false, length = 1000, comment = "意图词")
    private String description;

    @Column(name = "`schema`", comment = "入参规范")
    private String schema;

    @Column(name = "target", nullable = false, length = 1000, comment = "调用目标Bean")
    private String target;

    @Column(name = "remark", length = 500, comment = "备注")
    private String remark;

    @Column(name = "namespace", length = 40, comment = "命名空间")
    private String namespace;

    @Column(name = "ns_bundle", nullable = false, comment = "命名空间绑定 0:否 1:是")
    private Integer nsBundle;

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



}
