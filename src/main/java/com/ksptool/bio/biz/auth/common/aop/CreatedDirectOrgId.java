package com.ksptool.bio.biz.auth.common.aop;

import java.lang.annotation.*;

/**
 * 创建时自动设置所属直接组织ID
 * 如果字段为空则自动设置为当前用户直属组织ID
 * <p>
 * 直属组织ID: 当前用户直属组织ID(只能是公司,如果用户在集团企业下 直属就是集团，如果用户在集团的某个子公司下 直属就是子公司)
 *
 * @author KspTool
 * @since 1.6.21(U).90
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CreatedDirectOrgId {


}
