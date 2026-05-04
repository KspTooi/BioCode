package com.ksptool.bio.biz.auth.common.aop;

import java.lang.annotation.*;

/**
 * 🥰标准动态数据权限注解(RS 7级数据权限-2ID方案)
 * <p>
 * 标注在 Service 方法或类上，根据当前登录用户的 rsMax 权限级别动态过滤查询结果。
 * <p>
 * RS权限等级说明:
 * rsMax=0   全集团数据(直接放行,不启用过滤器)
 * rsMax=10  本公司+下级公司
 * rsMax=20  仅本公司
 * rsMax=30  本部门+下级部门
 * rsMax=40  仅本部门
 * rsMax=50  仅本人
 * rsMax=60  指定组织
 * rsMax=100 用户未配置任何数据权限组(拒绝所有数据)
 * <p>
 * 使用步骤:
 * 1.实体类继承 {@link RowScopePo}，业务表必须有 root_id、org_id、creator_id 字段
 * 2.在 Service 方法或类上添加此注解 {@link RowScope}
 * <p>
 * 注意: 拥有超级数据权限(*:*:*:*)的用户不受此过滤器约束，可查询全部数据。
 *
 * @author KspTool
 * @since 1.6.21(U).90
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RowScope {

    /**
     * 数据隔离模式
     * 默认为 FULL: 启用完整 RS 7级数据权限过滤
     */
    Mode mode() default Mode.FULL;

    enum Mode {
        /**
         * 完整 RS 数据权限过滤(默认): 租户 + 7级权限叠加
         */
        FULL,

        /**
         * 仅租户隔离: 只过滤 root_id, 忽略 7级 RS, 适用于组织树/字典等租户内全局可见的表
         */
        ROOT_ONLY,

        /**
         * 仅用户隔离: 只过滤 creator_id, 忽略 7级 RS, 适用于用户档案等用户内全局可见的表
         */
        USER_ONLY,
    }
}