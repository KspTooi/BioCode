package com.ksptool.bio.biz.auth.common.jpa;

import com.ksptool.bio.biz.auth.common.RowScopes;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * RowScopes 转换器
 * 用于JPA的转换器，它能够将 RowScopes 枚举类型转换为数据库中的整数类型，以及将数据库中的整数类型转换为 RowScopes 枚举类型
 *
 * @author KspTool
 * @since 1.6.23(W).54
 */
@Converter(autoApply = true)
public class RowScopesConv implements AttributeConverter<RowScopes, Integer> {

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
