package com.ksptool.bio.biz.auth.common;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * RowScopes 转换器
 * 用于JPA的转换器，它能够将 RowScopes 枚举类型转换为数据库中的整数类型，以及将数据库中的整数类型转换为 RowScopes 枚举类型
 *
 * @author KspTool
 * @since 2026-04-29
 */
@Converter(autoApply = true)
public class RowScopesConverter implements AttributeConverter<RowScopes, Integer> {

    @Override
    public Integer convertToDatabaseColumn(RowScopes attribute) {
        if (attribute == null) {
            return null;
        }
        return attribute.getCode();
    }

    @Override
    public RowScopes convertToEntityAttribute(Integer dbData) {
        if (dbData == null) {
            return null;
        }
        return RowScopes.of(dbData);
    }
}
