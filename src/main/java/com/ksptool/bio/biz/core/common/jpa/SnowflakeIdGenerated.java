package com.ksptool.bio.biz.core.common.jpa;

import org.hibernate.annotations.IdGeneratorType;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * 雪花算法ID生成器注解,加在你们PO类的ID字段上
 * <p>
 * 使用方式:
 * `@Id
 * `@SnowflakeGenerated
 * `@Column(name = "id", comment = "主键ID")
 * `private Long id;
 * <p>
 * 注意: 这个注解仅用于SpringDataJPA的ID字段。
 * <p>
 * ----请注意 使用这个注解时，请勿在新增时手动设置ID字段，否则会导致数据新增失败！
 *
 * @author KspTooi
 * @since 1.6.15(O).65
 */
@IdGeneratorType(SnowflakeIdGenerator.class) // 指向生成器实现类
@Retention(RUNTIME)
@Target({FIELD, METHOD})
public @interface SnowflakeIdGenerated {
}