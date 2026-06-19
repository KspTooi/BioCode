package com.ksptool.bio.biz.aacp.commons;

import lombok.Getter;

import java.util.List;
import java.util.Map;

import static com.ksptool.entities.Entities.as;

/**
 * 微函数参数解析器
 * 将输入JSON字符串解析为参数列表
 */
@Getter
public class MicroFuncParamResolver {

    //输入JSON字符串
    private final Map<String, Object> inputParams;

    //参数定义列表
    private final List<MicroFuncParamDef> defParams;


    /**
     * 构造函数
     *
     * @param inputParams 输入参数
     * @param defParams   参数定义
     */
    public MicroFuncParamResolver(Map<String, Object> inputParams, List<MicroFuncParamDef> defParams) {
        this.inputParams = inputParams;
        this.defParams = defParams;
    }

    public static MicroFuncParamResolver of(Map<String, Object> inputParams, List<MicroFuncParamDef> defParams) {
        return new MicroFuncParamResolver(inputParams, defParams);
    }


    /**
     * 解析参数
     *
     * @return 参数数组
     */
    public Object[] resolve() {

        Object[] params = new Object[defParams.size()];

        for (var def : defParams) {

            var idx = def.getIndex();

            //如果输入中没有参数定义的参数名，则跳过
            if (!inputParams.containsKey(def.getName())) {
                continue;
            }

            var pFip = inputParams.get(def.getName());

            // 如果值已经是目标类型实例，直接透传
            if (def.getType().isInstance(pFip)) {
                params[idx] = pFip;
                continue;
            }

            // 基本类型转换：String → 包装类型、Number → 包装类型
            Object converted = convertBasicType(pFip, def.getType());
            if (converted != null) {
                params[idx] = converted;
                continue;
            }

            // 否则尝试 as 映射（适用于 DTO 等复杂类型）
            params[idx] = as(pFip, def.getType());
        }

        return params;
    }


    public Object getParam(String name) {
        return inputParams.get(name);
    }

    public boolean hasParam(String name) {
        return inputParams.containsKey(name);
    }

    /** 将 String 或 Number 转换为指定的基本包装类型，不匹配时返回 null */
    private static Object convertBasicType(Object value, Class<?> targetType) {
        if (value == null) {
            return null;
        }
        if (value instanceof String s) {
            if (targetType == Long.class || targetType == long.class) {
                return Long.valueOf(s);
            }
            if (targetType == Integer.class || targetType == int.class) {
                return Integer.valueOf(s);
            }
            if (targetType == Double.class || targetType == double.class) {
                return Double.valueOf(s);
            }
            if (targetType == Boolean.class || targetType == boolean.class) {
                return Boolean.valueOf(s);
            }
        }
        if (value instanceof Number n) {
            if (targetType == Long.class || targetType == long.class) {
                return n.longValue();
            }
            if (targetType == Integer.class || targetType == int.class) {
                return n.intValue();
            }
            if (targetType == Double.class || targetType == double.class) {
                return n.doubleValue();
            }
        }
        return null;
    }

}
