package com.ksptool.bio.biz.aacp.commons;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 微函数元信息：@MicroFunc 注解元数据 + 反射调用句柄（对应 QT 的 QMetaMethod）
 */
@Getter
@Schema(description = "微函数元信息")
public class MicroFuncDef {

    @Schema(description = "微函数名称")
    private final String name;

    @Schema(description = "微函数唯一标识")
    private final String target;

    @Schema(description = "微函数描述")
    private final String description;

    @Schema(description = "所属 Spring Bean 实例")
    private final Object bean;

    @Schema(description = "目标方法")
    private final Method method;

    @Schema(description = "方法参数定义列表（名称 + 类型，来自 @Param 注解）")
    private final MicroFuncParamDef[] parameters;

    public MicroFuncDef(String target, String name, String description, Object bean, Method method) {
        this.target = target;
        this.name = name;
        this.description = description;
        this.bean = bean;
        this.method = method;
        Parameter[] javaParams = method.getParameters();
        MicroFuncParamDef[] defParams = new MicroFuncParamDef[javaParams.length];
        for (int i = 0; i < javaParams.length; i++) {
            defParams[i] = MicroFuncParamDef.of(javaParams[i], i);
        }
        this.parameters = defParams;
    }

    /**
     * 静态工厂方法，创建 MicroFuncDefinition 实例。
     *
     * @param target      微函数唯一标识
     * @param name        微函数名称
     * @param description 微函数描述
     * @param bean        所属 Spring Bean 实例
     * @param method      目标方法
     * @return 新实例
     */
    public static MicroFuncDef of(String target, String name, String description, Object bean, Method method) {
        return new MicroFuncDef(target, name, description, bean, method);
    }

    /**
     * 反射调用目标方法
     *
     * @param args 已由 MicroFuncService 完成 DTO 反序列化注入的参数数组
     * @return 方法返回值
     */
    public Object invoke(Object... args) throws Exception {
        return method.invoke(bean, args);
    }

    /**
     * 生成 JSON Schema 格式的入参规范（MCP tools/list inputSchema）
     *
     * @return inputSchema Map，包含 type、properties、required
     */
    public Map<String, Object> getInputSchema() {
        Map<String, Object> schema = new LinkedHashMap<>();
        schema.put("type", "object");

        Map<String, Object> properties = new LinkedHashMap<>();
        List<String> required = new ArrayList<>();

        for (int i = 0; i < parameters.length; i++) {
            Class<?> paramType = parameters[i].getType();
            String paramName = parameters[i].getName();

            properties.put(paramName, resolveTypeToSchema(paramType));
            if (paramType.isPrimitive()
                    || paramType == String.class
                    || paramType == Long.class || paramType == Integer.class || paramType == Short.class
                    || paramType == Double.class || paramType == Float.class
                    || paramType == Boolean.class) {
                required.add(paramName);
            }
        }

        schema.put("properties", properties);
        if (!required.isEmpty()) {
            schema.put("required", required);
        }
        return schema;
    }

    //Java类型→JSON Schema（自递归，无法内联）

    private Map<String, Object> resolveTypeToSchema(Class<?> type) {
        Map<String, Object> prop = new LinkedHashMap<>();

        if (type == String.class) {
            prop.put("type", "string");
            return prop;
        }
        if (type == Long.class || type == long.class
                || type == Integer.class || type == int.class
                || type == Short.class || type == short.class) {
            prop.put("type", "integer");
            return prop;
        }
        if (type == Double.class || type == double.class
                || type == Float.class || type == float.class) {
            prop.put("type", "number");
            return prop;
        }
        if (type == Boolean.class || type == boolean.class) {
            prop.put("type", "boolean");
            return prop;
        }
        if (type == List.class || type.isArray()) {
            prop.put("type", "array");
            return prop;
        }
        if (type == Map.class) {
            prop.put("type", "object");
            return prop;
        }

        //反射Dto字段
        prop.put("type", "object");
        Map<String, Object> fieldProperties = new LinkedHashMap<>();
        for (Field field : type.getDeclaredFields()) {
            if (Modifier.isStatic(field.getModifiers())) {
                continue;
            }
            Map<String, Object> fieldProp;
            Class<?> fieldType = field.getType();
            if (fieldType == List.class) {
                fieldProp = new LinkedHashMap<>();
                fieldProp.put("type", "array");
                Type genericType = field.getGenericType();
                if (genericType instanceof ParameterizedType pt) {
                    Type itemType = pt.getActualTypeArguments()[0];
                    if (itemType instanceof Class<?> itemClass) {
                        fieldProp.put("items", resolveTypeToSchema(itemClass));
                    }
                }
            } else {
                fieldProp = resolveTypeToSchema(fieldType);
            }
            fieldProperties.put(field.getName(), fieldProp);
        }
        prop.put("properties", fieldProperties);
        return prop;
    }
}
