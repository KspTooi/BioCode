package com.ksptool.bio.biz.core.common.jpa;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.HashSet;
import java.util.Set;

import static com.ksptool.entities.Entities.fromJsonArray;
import static com.ksptool.entities.Entities.toJson;

/**
 * 这是用于SpringDataJPA的转换器，它负责将PO中的Set<String>类型转换为数据库中的JSON字符串，
 * 并将数据库中的JSON字符串转换回PO中的Set<String>类型。
 * <p>
 * 使用方式:
 * 在PO类的某个字段上加入注解 @Convert(converter = SetStringConv.class)
 *
 * @author KspTooi
 * @since 1.6.15(O).65
 */
@Converter
public class SetStringConv implements AttributeConverter<Set<String>, String> {

    /**
     * 将Set<String>类型转换为数据库中的JSON字符串
     */
    @Override
    public String convertToDatabaseColumn(Set<String> strings) {
        // 这里使用Assembly的工具类来将List<String>类型转换为JSON字符串(如有需要可以替换为Gson、Jackson等其他JSON库)
        return toJson(strings);
    }

    /**
     * 将数据库中的JSON字符串转换回PO中的Set<String>类型
     */
    @Override
    public Set<String> convertToEntityAttribute(String s) {
        // 这里使用Assembly的工具类来将JSON字符串转换回Set<String>类型(如有需要可以替换为Gson、Jackson等其他JSON库)
        Set<String> strings = new HashSet<>();
        strings.addAll(fromJsonArray(s, String.class));
        return strings;
    }
}
