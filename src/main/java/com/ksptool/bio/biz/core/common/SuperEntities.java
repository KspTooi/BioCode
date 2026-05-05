package com.ksptool.bio.biz.core.common;

import lombok.Getter;

/**
 * 超级实体枚举
 * 超级实体是指系统内置的超级租户、超级用户、超级组、超级权限、超级数据权限
 * 这些实体会在系统冷启动时自动创建，它们拥有整个系统中的最高权限+最高数据权限且不受任何限制
 *
 * @author KspTool
 * @since 1.6.21(U).90
 */
@Getter
public enum SuperEntities {

    //超级租户 ID=-1
    ROOT(-1L, null),

    //超级用户 ID=-1 隶属于超级租户
    USER(-1L, null),

    //超级组 ID=-1 隶属于超级租户
    GROUP(-1L, null),

    //超级功能权限(SA) 通配符 *:*:*
    PERMISSION(null, "*:*:*"),

    //超级数据权限(SR) 通配符 *:*:*:*
    RS_PERMISSION(null, "*:*:*:*");

    //超级实体的固定ID (权限类为null)
    private final Long id;

    //超级权限的Code (非权限类为null)
    private final String code;

    /**
     * 构造函数
     *
     * @param id   超级实体的固定ID (权限类为null)
     * @param code 超级权限的Code (非权限类为null)
     */
    SuperEntities(Long id, String code) {
        this.id = id;
        this.code = code;
    }

}
