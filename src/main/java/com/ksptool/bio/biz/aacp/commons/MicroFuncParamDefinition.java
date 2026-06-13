package com.ksptool.bio.biz.aacp.commons;

import java.lang.reflect.Parameter;

import com.ksptool.bio.biz.aacp.commons.annotation.Param;

/**
 * 微函数参数定义
 */
public class MicroFuncParamDefinition {

    //参数名称
    private final String name;

    //参数类型
    private final Class<?> type;

    //参数索引
    private final Integer index;


    public MicroFuncParamDefinition(Parameter param,Integer index) {

        if(param == null){
            throw new IllegalArgumentException("参数不能为空");
        }

        Param anno = param.getAnnotation(Param.class);
        this.name = anno != null ? anno.value() : param.getName();
        this.type = param.getType();
        this.index = index;
    }
    
    public MicroFuncParamDefinition(String name, Class<?> type,Integer index) {
        this.name = name;
        this.type = type;
        this.index = index;
    }

    public static MicroFuncParamDefinition of(Parameter param,Integer index) {
        return new MicroFuncParamDefinition(param, index);
    }
    
    public static MicroFuncParamDefinition of(String name, Class<?> type,Integer index) {
        return new MicroFuncParamDefinition(name, type, index);
    }

    /**
     * 返回参数名（来自 @Param 注解 value，无注解时回退到反射默认名）。
     *
     * @return 参数名
     */
    public String getName() {
        return name;
    }

    /**
     * 返回参数 Java 类型。
     *
     * @return 参数类型
     */
    public Class<?> getType() {
        return type;
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