package com.ksptool.bio.biz.aacp.commons.annotation;

import java.lang.annotation.*;

/**
 * 微函数参数注解：标注在 @MicroFunc 方法的参数上，为 AI Agent 提供语义化参数名。
 * <p>
 * 只要方法有参数，每个参数必须标注此注解，否则注册时拒绝并打 error 日志。
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Param {

    //参数名，如 "a"、"url"、"message"
    String value();
    
}