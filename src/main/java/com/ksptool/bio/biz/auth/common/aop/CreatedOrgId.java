package com.ksptool.bio.biz.auth.common.aop;

import java.lang.annotation.*;

/**
 * 创建时自动设置最细粒度组织ID
 * 如果字段为空则自动设置为当前用户的最细粒度组织架构ID
 * 
 * 最细粒度组织ID: 当前用户最细粒度组织架构ID(可以是公司,也可以是部门) 
 * 如果用户在企业的某个部门下，最细粒度组织就是部门
 * 如果用户在企业的某个公司下，最细粒度组织就是公司
 * 
 * @author KspTool
 * @since 2026-04-29
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CreatedOrgId {


}
