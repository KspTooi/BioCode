package com.ksptool.bio.biz.auth.common.aop;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Transient;

import org.hibernate.annotations.Filter;


/**
 * 用户级数据权限过滤器(仅按创建者隔离)
 * <p>
 * 适用于只需要 USER_ONLY 模式({@link RowScope.Mode#USER_ONLY})的表，
 * 业务表只需要有 creator_id 字段即可，无需 root_id 和 org_id。
 * <p>
 * 依赖 {@link RowScopePo} 上声明的全局 {@code @FilterDef(name = "rsFilter", ...)}，
 * 共用同一个 filter 名称，{@link RowScopeAspect} 无需任何改动。
 *
 * @author KspTool
 * @see RowScopePo
 * @see RowScope.Mode#USER_ONLY
 */
@MappedSuperclass
@Filter(
        name = "rsFilter",
        condition = "creator_id = :userId"
)
public abstract class RowScopeUserOnlyPo {

    @Transient
    @Column(name = "creator_id", nullable = false, updatable = false, comment = "创建者ID")
    private Long creatorId;

}
