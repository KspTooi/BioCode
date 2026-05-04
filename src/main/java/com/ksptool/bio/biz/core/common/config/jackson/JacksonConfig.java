package com.ksptool.bio.biz.core.common.config.jackson;

import org.springframework.boot.jackson.autoconfigure.JsonMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.ext.javatime.ser.LocalDateTimeSerializer;
import tools.jackson.databind.module.SimpleModule;
import tools.jackson.databind.ser.std.ToStringSerializer;

import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.TimeZone;

/**
 * Jackson全局配置 (Spring Boot 4.0 / Jackson 3 适配版)
 * Long/BigInteger -> String (解决前端精度丢失)
 * LocalDateTime -> yyyy-MM-dd HH:mm:ss
 *
 * @author KspTooi
 * @since 1.0.1(A).1
 */
@Configuration
public class JacksonConfig {

    private static final String DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss";

    @Bean
    public JsonMapperBuilderCustomizer jsonMapperBuilderCustomizer() {
        return builder -> {
            //全局基础配置
            builder.defaultTimeZone(TimeZone.getTimeZone("GMT+8"));
            builder.defaultDateFormat(new SimpleDateFormat(DATE_TIME_PATTERN));

            //禁用 "遇到未知属性报错"
            builder.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

            //创建自定义模块
            SimpleModule simpleModule = new SimpleModule();

            //添加传出方向(Vo)的格式化器
            simpleModule.addSerializer(Long.class, ToStringSerializer.instance);
            simpleModule.addSerializer(Long.TYPE, ToStringSerializer.instance);
            simpleModule.addSerializer(BigInteger.class, ToStringSerializer.instance);
            simpleModule.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(DateTimeFormatter.ofPattern(DATE_TIME_PATTERN)));


            //添加传入方向(Dto)的格式化器
            simpleModule.addDeserializer(LocalDateTime.class, new JacksonLdtInputConv());

            //将模块注册到 Builder
            builder.addModule(simpleModule);
        };
    }


}
