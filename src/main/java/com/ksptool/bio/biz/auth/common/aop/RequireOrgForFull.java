package com.ksptool.bio.biz.auth.common.aop;

import java.lang.annotation.*;

/**
 * 组织必填注解
 * <p>
 * 标注在 Service 方法上，用于在 FULL 数据权限模式下新增数据时验证用户必须有部门或公司
 * <p>
 * 验证规则:
 * - FULL 模式: 新增时用户必须要有 orgId(部门或公司)，否则抛出异常
 * - ROOT_ONLY 模式: 跳过验证，用户可以没有部门
 * - USER_ONLY 模式: 跳过验证，用户可以没有部门
 * <p>
 * 使用场景:
 * 适用于标注了 {@link RowScope} 且模式为 FULL 的 Service 新增方法
 *
 * @author KspTool
 * @since 1.6.30
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequireOrgForFull {
}