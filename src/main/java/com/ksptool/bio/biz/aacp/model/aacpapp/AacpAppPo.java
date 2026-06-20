package com.ksptool.bio.biz.aacp.model.aacpapp;

import com.ksptool.bio.biz.auth.common.aop.CreatedRootId;
import com.ksptool.bio.biz.auth.common.aop.RowScopeRootOnlyPo;
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
@Table(name = "aacp_app")
@EntityListeners({AuditingEntityListener.class, RsAuditingEntityListener.class})
@SQLDelete(sql = "UPDATE aacp_app SET delete_time = NOW() WHERE id = ?")
@SQLRestriction("delete_time IS NULL")
public class AacpAppPo extends RowScopeRootOnlyPo {

    @Id
    @SnowflakeIdGenerated
    @Column(name = "id", nullable = false, comment = "主键ID")
    private Long id;

    @CreatedRootId
    @Column(name = "root_id", nullable = false, comment = "租户ID")
    private Long rootId;

    @Column(name = "name", nullable = false, length = 40, comment = "应用名称")
    private String name;

    @Column(name = "code", nullable = false, length = 16, comment = "应用代码")
    private String code;

    @Column(name = "app_key", nullable = false, length = 2048, comment = "访问密钥")
    private String appKey;

    @Column(name = "is_public", nullable = false, columnDefinition = "TINYINT", comment = "是否公开 0:不公开 1:公开")
    private Integer isPublic;

    @Column(name = "ips", nullable = false, columnDefinition = "JSON", comment = "IP白名单列表")
    private String ips;

    @Column(name = "remark", length = 200, comment = "备注")
    private String remark;

    @Column(name = "status", nullable = false, columnDefinition = "TINYINT", comment = "状态 0:禁用 1:启用")
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

}