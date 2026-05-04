package com.ksptool.bio.biz.core.common.jpa;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.HashSet;
import java.util.Set;

import static com.ksptool.entities.Entities.fromJsonArray;
import static com.ksptool.entities.Entities.toJson;

/**
 * 这是用于SpringDataJPA的转换器，它负责将PO中的Set<Long>类型转换为数据库中的JSON字符串，
 * 并将数据库中的JSON字符串转换回PO中的Set<Long>类型。
 * <p>
 * 使用方式:
 * 在PO类的某个字段上加入注解 @Convert(converter = SetLongConv.class)
 *
 * @author KspTooi
 * @since 1.6.15(O).65
 */
@Converter
public class SetLongConv implements AttributeConverter<Set<Long>, String> {

    /**
     * 将Set<Long>类型转换为数据库中的JSON字符串
     */
    @Override
    public String convertToDatabaseColumn(Set<Long> longs) {
        return toJson(longs);
    }

    /**
     * 将数据库中的JSON字符串转换回PO中的Set<Long>类型
     */
    @Override
    public Set<Long> convertToEntityAttribute(String s) {
        Set<Long> longs = new HashSet<>();
        longs.addAll(fromJsonArray(s, Long.class));
        return longs;
    }
}
