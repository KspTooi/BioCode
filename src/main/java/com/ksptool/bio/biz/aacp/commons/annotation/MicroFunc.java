package com.ksptool.bio.biz.aacp.commons.annotation;

import java.lang.annotation.*;

/**
 * 微函数注解：标注在 Spring Bean 的 public 方法上，启动时自动扫描并注册到微函数注册中心
 * <p>
 * 标注此注解的方法将作为一个"能力点"暴露给 MCP 协议，外部 AI 客户端可通过 tools/call 远程调用。
 * 方法参数支持任意 Java POJO（含 Dto），调用时自动完成 JSON → Dto 的反序列化注入。
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface MicroFunc {

    /**
     * 微函数唯一标识，如 "user.query"、"order.create"
     */
    String target();

    /**
     * 微函数名称（简短中文描述）
     */
    String name();

    /**
     * 微函数描述，供 AI 进行意图匹配
     */
    String description();
}
