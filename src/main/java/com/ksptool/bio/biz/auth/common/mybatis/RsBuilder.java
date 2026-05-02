package com.ksptool.bio.biz.auth.common.mybatis;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Mybatis数据权限构建器(RS 7级数据权限-2ID方案)
 * 用于构建Mybatis查询时的数据权限SQL
 * <p>
 * 租户隔离原则:
 * 在没有超级数据权限的情况下,任何查询都必须强制带上 root_id = :rootId 作为硬底线
 * 即使 rsMax=0 (全集团数据) 也只能看到本租户的全部数据,而非真正跨租户的全部数据
 * 当上下文为空(说明持有超级数据权限或未启用@RowScope)时直接放行,允许跨租户
 * <p>
 * RS权限等级与SQL过滤策略 (所有分支均强制叠加 root_id = ...):
 * rsMax=0   本租户全部数据    按 root_id = ...                               过滤
 * rsMax=10  本公司+下级公司   按 root_id = ... AND org_id IN(...)            过滤
 * rsMax=20  仅本公司          按 root_id = ... AND org_id IN(...)            过滤
 * rsMax=30  本部门+下级部门   按 root_id = ... AND org_id IN(...)            过滤
 * rsMax=40  仅本部门          按 root_id = ... AND org_id IN(...)            过滤
 * rsMax=50  仅本人            按 root_id = ... AND creator_id = ...          过滤
 * rsMax=60  指定组织          按 root_id = ... AND org_id IN(...)            过滤
 * <p>
 * 使用方法:
 * 1.在Service函数上面加入@RowScope注解
 * 2.在Mapper上加一个参数 rsSql 用于接收数据权限SQL，或者直接加在Dto中
 * 3.在Mapper.xml中使用 ${rsSql} 来使用数据权限SQL 加在Where后面
 *
 * @author KspTool
 */
public class RsBuilder {

    /**
     * 构建Mybatis查询时的数据权限SQL
     *
     * @param alias 主表的别名 例如有SQL如下 SELECT * FROM 表名 AS T 这里就传T
     * @return 数据权限SQL
     */
    public static String build(String alias) {

        //上下文为空，说明没有加@RowScope注解或者当前用户拥有超级数据权限，直接放行(允许跨租户)
        RsContext context = RsContextHolder.get();
        if (context == null) {
            return "";
        }

        Integer rsMax = context.getRsMax();

        //rsMax 为 null 表示权限配置异常，拒绝所有数据
        if (rsMax == null) {
            return " AND 1 = 0 ";
        }

        //rootId 为 null 表示用户未绑定租户，无法做租户隔离，拒绝所有数据
        Long rootId = context.getRootId();
        if (rootId == null) {
            return " AND 1 = 0 ";
        }

        //alias 为空时不加前缀，否则加 "alias." 前缀
        String prefix = "";
        if (alias != null && !alias.isBlank()) {
            prefix = alias + ".";
        }

        //租户隔离硬底线: 所有分支都强制叠加 root_id = ...
        String tenantClause = " AND " + prefix + "root_id = " + rootId + " ";

        //rsMax = 0 本租户全部数据，仅按租户过滤
        if (rsMax == 0) {
            return tenantClause;
        }

        //rsMax = 50 仅本人，租户 + creator_id 过滤
        if (rsMax == 50) {
            return tenantClause + " AND " + prefix + "creator_id = " + context.getUserId() + " ";
        }

        //rsMax = 10/20/30/40/60 均按租户 + org_id IN(...) 过滤
        if (rsMax == 10 || rsMax == 20 || rsMax == 30 || rsMax == 40 || rsMax == 60) {
            List<Long> orgIds = context.getOrgIds();

            //允许的组织列表为空，拒绝所有数据
            if (orgIds == null || orgIds.isEmpty()) {
                return " AND 1 = 0 ";
            }

            String ids = orgIds.stream()
                    .map(String::valueOf)
                    .collect(Collectors.joining(","));
            return tenantClause + " AND " + prefix + "org_id IN (" + ids + ") ";
        }

        //未知的 rsMax 值(包括 100 用户未配置任何数据权限组)，拒绝所有数据
        return " AND 1 = 0 ";
    }

    /**
     * 构建Mybatis查询时的数据权限SQL（不带表别名）
     *
     * @return 数据权限SQL
     */
    public static String build() {
        return build(null);
    }
}
