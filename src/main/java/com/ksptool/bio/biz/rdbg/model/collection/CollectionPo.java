package com.ksptool.bio.biz.rdbg.model.collection;

import com.ksptool.assembly.entity.exception.AuthException;
import com.ksptool.bio.commons.utils.IdWorker;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;
import java.util.List;

import static com.ksptool.bio.biz.auth.service.SessionService.session;


@Getter
@Setter
@Entity
@Table(name = "rdbg_collection", comment = "请求集合表")
@SQLDelete(sql = "UPDATE rdbg_collection SET delete_time = now() WHERE id = ?")
@SQLRestriction("delete_time IS NULL")
public class CollectionPo {

    @Id
    @Column(name = "id", comment = "主键ID")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id", nullable = true, foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT), comment = "父级节点 NULL顶级")
    private CollectionPo parent;

    @Column(name = "company_id", nullable = false, comment = "公司ID")
    private Long companyId;

    @Column(name = "name", nullable = false, length = 128, comment = "集合名称")
    private String name;

    @Column(name = "kind", nullable = false, columnDefinition = "tinyint", comment = "集合类型 0:请求 1:组")
    private Integer kind;

    @Column(name = "req_url", length = 320, comment = "请求URL")
    private String reqUrl;

    @Column(name = "req_url_params_json", columnDefinition = "json", comment = "请求URL参数JSON")
    private String reqUrlParamsJson;

    @Column(name = "req_method", columnDefinition = "tinyint", comment = "请求方法 0:GET 1:POST 2:PUT 3:PATCH 4:DELETE 5:HEAD 6:OPTIONS")
    private Integer reqMethod;

    @Column(name = "req_header_json", columnDefinition = "json", comment = "请求头JSON")
    private String reqHeaderJson;

    @Column(name = "req_body_kind", columnDefinition = "tinyint", comment = "请求体类型 0:空 1:form-data 2:raw-text 3:raw-javascription 4:raw-json 5:raw-html 6:raw-xml 7:binary 8:x-www-form-urlencoded")
    private Integer reqBodyKind;

    @Column(name = "req_body_json", columnDefinition = "json", comment = "请求体JSON")
    private String reqBodyJson;

    @Column(name = "seq", nullable = false, comment = "排序")
    private Integer seq;

    @Column(name = "create_time", nullable = false, comment = "创建时间")
    private LocalDateTime createTime;

    @Column(name = "creator_id", nullable = false, comment = "创建人ID")
    private Long creatorId;

    @Column(name = "update_time", nullable = false, comment = "更新时间")
    private LocalDateTime updateTime;

    @Column(name = "updater_id", nullable = false, comment = "更新人ID")
    private Long updaterId;

    @Column(name = "delete_time", nullable = true, comment = "删除时间 为NULL未删除")
    private LocalDateTime deleteTime;


    @BatchSize(size = 100)
    @OrderBy("seq ASC")
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "parent", cascade = CascadeType.ALL)
    private List<CollectionPo> children;

    @PrePersist
    private void onCreate() throws AuthException {
        if (this.id == null) {
            this.id = IdWorker.nextId();
        }

        LocalDateTime now = LocalDateTime.now();
        if (this.createTime == null) {
            this.createTime = now;
        }
        if (this.updateTime == null) {
            this.updateTime = this.createTime;
        }

        if (this.creatorId == null) {
            this.creatorId = session().getUserId();
        }
        if (this.updaterId == null) {
            this.updaterId = session().getUserId();
        }
        if (this.companyId == null) {
            this.companyId = session().getRootId();
        }

    }

    @PreUpdate
    private void onUpdate() throws AuthException {
        this.updateTime = LocalDateTime.now();
        if (this.updaterId == null) {
            this.updaterId = session().getUserId();
        }
    }
}
