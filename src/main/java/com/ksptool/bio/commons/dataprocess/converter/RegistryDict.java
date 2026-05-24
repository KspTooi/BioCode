package com.ksptool.bio.commons.dataprocess.converter;

import java.lang.annotation.*;

/**
 * 注册表字典映射注解
 * 通过注册表keyPath动态查找条目列表，实现Integer值与中文标签的双向映射
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface RegistryDict {

    /**
     * 注册表节点的全路径
     * 该节点下的子条目将作为字典项，nkey作为编码，label作为显示文本
     */
    String keyPath();

    /**
     * 当注册表中找不到匹配项时，是否使用硬编码format作为后备映射
     * 格式: "0=正常;1=停用"
     */
    String fallback() default "";

    /**
     * 是否必填
     */
    boolean required() default false;

    /**
     * 错误信息
     */
    String message() default "";
}
