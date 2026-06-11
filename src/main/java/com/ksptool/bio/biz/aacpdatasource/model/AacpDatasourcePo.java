package com.ksptool.bio.biz.aacpdatasource.model;

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
@Table(name = "aacp_datasource")
@EntityListeners(AuditingEntityListener.class)
@SQLDelete(sql = "UPDATE aacp_datasource SET delete_time = NOW() WHERE id = ?")
@SQLRestriction("delete_time IS NULL")
public class AacpDatasourcePo {

    @Id
    @SnowflakeIdGenerated
    @Column(name = "id", nullable = false, comment = "主键ID")
    private Long id;

    @Column(name = "root_id", nullable = false, comment = "租户ID")
    private Long rootId;

    @Column(name = "name", nullable = false, length = 40, comment = "数据源名称")
    private String name;

    @Column(name = "code", nullable = false, length = 32, comment = "数据源编码")
    private String code;

    @Column(name = "kind", nullable = false, comment = "数据源类型 0:MYSQL")
    private Integer kind;

    @Column(name = "drive", nullable = false, length = 200, comment = "JDBC驱动")
    private String drive;

    @Column(name = "url", nullable = false, comment = "连接字符串")
    private String url;

    @Column(name = "username", length = 200, comment = "连接用户名")
    private String username;

    @Column(name = "password", length = 2000, comment = "连接密码")
    private String password;

    @Column(name = "default_db", nullable = false, length = 200, comment = "默认数据库")
    private String defaultDb;

    @Column(name = "query_max_rows", nullable = false, comment = "最大查询行数")
    private Integer queryMaxRows;

    @Column(name = "execute_batch", nullable = false, comment = "是否支持批处理 0:不支持 1:支持")
    private Integer executeBatch;

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
