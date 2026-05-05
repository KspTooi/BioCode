package com.ksptool.bio.biz.core.common.jpa;

import com.ksptool.bio.biz.core.common.Switch;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * 用于SpringDataJPA的转换器，负责将PO中的Switch类型转换为数据库中的Integer，反之亦然。
 * <p>
 * 使用方式:
 * 在PO类的某个字段上加入注解 {@code @Convert(converter = SwitchConv.class)}
 *
 * @author KspTooi
 * @since 1.6.21(U).90
 */
@Converter
public class SwitchConv implements AttributeConverter<Switch, Integer> {

    @Override
    public Integer convertToDatabaseColumn(Switch sw) {
        if (sw == null) {
            return null;
        }
        return sw.getValue();
    }

    @Override
    public Switch convertToEntityAttribute(Integer value) {
        if (value == null) {
            return null;
        }
        return new Switch(value);
    }
}
