package com.ksptool.bio.biz.auth.common.aop;

import java.lang.annotation.*;

/**
 * 系统内置数据权限注解(isSystem 字段过滤)
 * <p>
 * 标注在 Service 方法或类上，根据当前登录用户是否持有透视权限(PERSP)，
 * 决定是否在 Hibernate 查询时启用 isSystem 字段过滤器。
 * <p>
 * 过滤策略:
 * - 持有超级数据权限(*:*:*:*)  : 直接放行，不启用过滤器，可查询全部数据(含 isSystem=true)
 * - 持有透视权限(*:*:*:*:PS)   : 直接放行，不启用过滤器，可查询全部数据(含 isSystem=true)
 * - 无以上任意权限              : 启用过滤器，只能查询到 isSystem = false 的数据
 * <p>
 * 使用步骤:
 * 1. 实体类继承 {@link SystemScopePo}，业务表必须有 is_system 字段
 * 2. 在 Service 方法或类上添加此注解 {@link SystemScope}
 *
 * @author KspTool
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface SystemScope {
}
