package com.ksptool.bio.biz.aacp.model.provider;

import com.ksptool.bio.biz.auth.common.aop.RowScopeRootOnlyPo;
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
@Table(name = "aacp_provider")
@EntityListeners(AuditingEntityListener.class)
@SQLDelete(sql = "UPDATE aacp_provider SET delete_time = NOW() WHERE id = ?")
@SQLRestriction("delete_time IS NULL")
public class AacpProviderPo extends RowScopeRootOnlyPo {

    @Id
    @SnowflakeIdGenerated
    @Column(name = "id", nullable = false, comment = "主键ID")
    private Long id;

    @Column(name = "root_id", nullable = false, comment = "租户ID")
    private Long rootId;

    @Column(name = "name", nullable = false, length = 80, comment = "供应商名称")
    private String name;

    @Column(name = "code", nullable = false, length = 32, comment = "供应商代码")
    private String code;

    @Column(name = "api_kind", nullable = false, columnDefinition = "TINYINT", comment = "接口类型 0:OpenAi 1:Anthropic")
    private Integer apiKind;

    @Column(name = "api_key", length = 2000, comment = "接口密钥")
    private String apiKey;

    @Column(name = "api_host", nullable = false, length = 512, comment = "接口地址")
    private String apiHost;

    @Column(name = "api_url", nullable = false, length = 512, comment = "接口端点")
    private String apiUrl;

    @Column(name = "proxy_kind", nullable = false, columnDefinition = "TINYINT", comment = "代理类型 0:无 1:HTTP 2:SOCKS5")
    private Integer proxyKind;

    @Column(name = "proxy_url", length = 512, comment = "代理地址")
    private String proxyUrl;

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