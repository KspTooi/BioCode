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


    public MicroFuncParamDefinition(Parameter param) {

        if(param == null){
            throw new IllegalArgumentException("参数不能为空");
        }

        Param anno = param.getAnnotation(Param.class);
        this.name = anno != null ? anno.value() : param.getName();
        this.type = param.getType();
    }
    
    public MicroFuncParamDefinition(String name, Class<?> type) {
        this.name = name;
        this.type = type;
    }

    public static MicroFuncParamDefinition of(Parameter param) {
        return new MicroFuncParamDefinition(param);
    }
    
    public static MicroFuncParamDefinition of(String name, Class<?> type) {
        return new MicroFuncParamDefinition(name, type);
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
}