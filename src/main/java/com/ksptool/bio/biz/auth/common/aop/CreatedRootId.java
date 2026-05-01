package com.ksptool.bio.biz.auth.common.aop;

import java.lang.annotation.*;

/**
 * 创建时自动设置所属租户ID
 * 如果字段为空则自动设置为当前用户所属租户ID
 * 
 * @author KspTool
 * @since 2026-04-29
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CreatedRootId {
}
