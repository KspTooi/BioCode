package com.ksptool.bio.biz.auth.common.mybatis;

import com.ksptool.bio.biz.auth.common.RowScopes;
import lombok.Getter;
import lombok.Setter;


import java.util.List;

/**
 * Mybatis数据权限上下文(RS 7级数据权限-2ID方案)
 * 用于在Mybatis查询时根据用户权限过滤数据
 *
 * @author KspTool
 */
@Getter
@Setter
public class RsContext {

    private RowScopes rsMax;
    private Long userId;
    private Long rootId;
    private List<Long> orgIds;

    /**
     * 构造函数
     *
     * @param rsMax  数据权限最大等级
     * @param userId 用户ID
     * @param rootId 所属企业(租户)ID
     * @param orgIds 允许访问的组织IDS(可公司、可部门等组织树节点)
     */
    public RsContext(RowScopes rsMax, Long userId, Long rootId, List<Long> orgIds) {
        this.rsMax = rsMax;
        this.userId = userId;
        this.rootId = rootId;
        this.orgIds = orgIds;
    }

}
