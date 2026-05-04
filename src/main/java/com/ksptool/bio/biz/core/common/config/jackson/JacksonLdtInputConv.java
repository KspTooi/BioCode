package com.ksptool.bio.biz.core.common.config.jackson;

import org.apache.commons.lang3.StringUtils;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.ValueDeserializer;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * LocalDateTime反序列化器(JSON字符串 -> Dto)
 * 这个类主要用来处理前端传进来的时间字符串，并将其转换为LDT对象。
 *
 * @author KspTooi
 * @since 1.0.1(A).1
 */
public class JacksonLdtInputConv extends ValueDeserializer<LocalDateTime> {

    /**
     * 传入方向(Dto)的多种格式支持
     * 支持的格式：
     * - 2000-01-01 12:00:00.123
     * - 2000-01-01 12:00:00
     * - 2000-01-01 12:00
     * - 2000-01-01 12
     */
    private static final DateTimeFormatter[] FORMATTERS = {
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"),
    };

    /**
     * 反序列化JSON字符串为LocalDateTime对象 这里主要用来把前端传入的字符串转换成LDT对象
     *
     * @param p    JsonParser 解析器
     * @param ctxt DeserializationContext 反序列化上下文
     * @return LocalDateTime对象
     * @throws JacksonException 如果反序列化失败
     */
    @Override
    public LocalDateTime deserialize(JsonParser p, DeserializationContext ctxt) throws JacksonException {

        //获取JSON字符串
        var value = p.getValueAsString();

        //如果JSON字符串为空，则返回null
        if (StringUtils.isBlank(value)) {
            return null;
        }

        //遍历FORMATTERS，尝试解析JSON字符串(主要是为了处理前端传入的多种格式的时间字符串)
        for (var formatter : FORMATTERS) {
            try {
                return LocalDateTime.parse(value, formatter);
            } catch (Exception ignored) {
            }
        }

        //如果遍历所有的FORMATTERS都没有解析成功，则抛出异常
        throw new RuntimeException("无法解析时间字符串: " + value);
    }

}
