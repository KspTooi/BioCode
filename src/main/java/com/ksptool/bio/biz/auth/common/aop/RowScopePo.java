package com.ksptool.bio.biz.auth.common.aop;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Transient;

import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;


/**
 * 完整行级数据权限过滤器(RS 7级数据权限-2ID方案)
 * 用于在Hibernate查询时根据用户权限过滤数据
 * <p>
 * 2ID方案: 业务表使用 root_id + org_id 两个ID表达数据归属
 * org_id 为最细粒度归属节点ID(可公司、可部门、可班组等组织树节点)
 * <p>
 * 租户隔离原则:
 * 在没有超级数据权限的情况下,任何查询都必须强制带上 root_id = :rootId 作为硬底线
 * 包括 rsMax=0 (全集团数据) 也只能看到本租户的全部数据,而非真正跨租户的全部数据
 * 真正的跨租户访问必须由超级数据权限(由 RowScopeAspect 直接放行不进入此过滤器)持有
 * <p>
 * RS权限等级与SQL过滤策略 (所有分支均强制叠加 root_id = :rootId):
 * 1. rsMax=0   本租户全部数据    按 root_id = :rootId 过滤
 * 2. rsMax=10  本公司+下级公司   按 root_id = :rootId AND org_id IN(:orgIds) 过滤
 * 3. rsMax=20  仅本公司          按 root_id = :rootId AND org_id IN(:orgIds) 过滤
 * 4. rsMax=30  本部门+下级部门   按 root_id = :rootId AND org_id IN(:orgIds) 过滤
 * 5. rsMax=40  仅本部门          按 root_id = :rootId AND org_id IN(:orgIds) 过滤
 * 6. rsMax=50  仅本人            按 root_id = :rootId AND creator_id = :userId 过滤
 * 7. rsMax=60  指定组织          按 root_id = :rootId AND org_id IN(:orgIds) 过滤
 * <p>
 * 请注意: 尽管在这个类中已经预定义了RS数据权限所必须的参数，但根据编码规范，继承此类的子类还需要显式声明这些参数。
 *
 * @author KspTool
 * @apiNote 在子类中需要显式声明 rootId、orgId 和 creatorId 字段，否则在查询时会报错
 */
@MappedSuperclass
@FilterDef(name = "rsFilter", parameters = {
        @ParamDef(name = "rootId", type = Long.class),
        @ParamDef(name = "orgIds", type = Long.class),
        @ParamDef(name = "userId", type = Long.class),
        @ParamDef(name = "rsMax", type = Integer.class)
})
@Filter(
        name = "rsFilter",
        condition = """
                root_id = :rootId
                AND (
                       ( :rsMax = 0 )
                    OR ( :rsMax IN (10, 20, 30, 40, 60) AND org_id IN (:orgIds) )
                    OR ( :rsMax = 50 AND creator_id = :userId )
                )
                """
)
public abstract class RowScopePo {

    @Transient
    @Column(name = "root_id", nullable = false, updatable = false, comment = "租户ID")
    private Long rootId;

    @Transient
    @Column(name = "org_id", nullable = false, updatable = false, comment = "直属组织ID(2ID方案下的最细粒度归属节点)")
    private Long orgId;

    @Transient
    @Column(name = "creator_id", nullable = false, updatable = false, comment = "创建者ID")
    private Long creatorId;

}
