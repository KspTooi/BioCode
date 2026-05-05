package com.ksptool.bio.biz.auth.common.aop;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Transient;

import org.hibernate.annotations.Filter;


/**
 * 租户级数据权限过滤器(仅按租户隔离)
 * <p>
 * 适用于只需要 ROOT_ONLY 模式({@link RowScope.Mode#ROOT_ONLY})的表，
 * 业务表只需要有 root_id 字段即可，无需 org_id。
 *
 * @author KspTool
 * @see RowScope.Mode#ROOT_ONLY
 * @since 1.6.24(X).21
 */
@MappedSuperclass
@Filter(
        name = "rsFilter",
        condition = "root_id = :rootId"
)
public abstract class RowScopeRootOnlyPo {

    @Transient
    @Column(name = "root_id", nullable = false, updatable = false, comment = "租户ID")
    private Long rootId;

}
