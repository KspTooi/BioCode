package com.ksptool.bio.biz.aacp.commons;

import com.ksptool.bio.biz.aacp.commons.annotation.Param;
import lombok.Getter;

import java.lang.reflect.Parameter;

/**
 * 微函数参数定义
 */
public class MicroFuncParamDef {

    //参数名称
    @Getter
    private final String name;

    //参数类型
    @Getter
    private final Class<?> type;

    //参数索引
    private final Integer index;


    public MicroFuncParamDef(Parameter param, Integer index) {

        if (param == null) {
            throw new IllegalArgumentException("参数不能为空");
        }

        Param anno = param.getAnnotation(Param.class);
        this.name = anno != null ? anno.value() : param.getName();
        this.type = param.getType();
        this.index = index;
    }

    public MicroFuncParamDef(String name, Class<?> type, Integer index) {
        this.name = name;
        this.type = type;
        this.index = index;
    }

    public static MicroFuncParamDef of(Parameter param, Integer index) {
        return new MicroFuncParamDef(param, index);
    }

    public static MicroFuncParamDef of(String name, Class<?> type, Integer index) {
        return new MicroFuncParamDef(name, type, index);
    }

    /**
     * 返回参数在方法签名中的索引位置。
     *
     * @return 索引（0-based）
     */
    public int getIndex() {
        return index;
    }
}