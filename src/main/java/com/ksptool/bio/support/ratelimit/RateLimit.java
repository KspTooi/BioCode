package com.ksptool.bio.support.ratelimit;

import java.lang.annotation.*;

/**
 * 限流注解
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RateLimit {

    /**
     * 业务代码,用于标识限流业务类型
     */
    String biz() default "";

    /**
     * 限流范围
     */
    RateLimitScope scope() default RateLimitScope.GLOBAL;

    /**
     * 限流次数(上限)
     */
    int period() default 60;

    /**
     * 限流周期,单位:秒
     */
    int limit() default 60;

    /**
     * 存储异常时是否放行 为true时放行，为false时不放行
     */
    boolean failOpen() default true;

}