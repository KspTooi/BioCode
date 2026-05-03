package com.ksptool.bio.biz.auth.common;

import java.util.Set;

/**
 * 行级数据权限计算结果
 * 用于存储用户行级数据权限计算结果
 * 
 *
 * @author KspTool
 * @since 2026-04-28
 */
public record RsCalculated(RowScopes rsMax, Set<Long> allowOrgIds) {

    /**
     * 构造函数
     *
     * @param rsMax 数据权限最大等级
     * @param allowOrgIds 允许访问的组织IDS(可部门可公司)
     * @return RsCalculated 行级数据权限计算结果
     */
    public static RsCalculated of(RowScopes rsMax, Set<Long> allowOrgIds) {
        return new RsCalculated(rsMax, allowOrgIds);
    }
}

