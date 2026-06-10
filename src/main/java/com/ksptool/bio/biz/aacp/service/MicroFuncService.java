package com.ksptool.bio.biz.aacp.service;

import com.google.gson.Gson;
import com.ksptool.bio.biz.aacp.commons.MicroFuncDefinition;
import com.ksptool.bio.biz.aacp.commons.MicroFuncRegistry;
import com.ksptool.bio.biz.aacp.commons.annotation.MicroFunc;
import com.ksptool.bio.biz.aacp.commons.jrpc.vo.ToolsCallVo;
import com.ksptool.bio.biz.aacp.commons.jrpc.vo.ToolsListVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

/**
 * 微函数业务逻辑：启动扫描 + DTO 注入调用 + Schema 生成（对应 QT 的 invoke + Q_ARG）
 * <p>
 * 在 ApplicationReadyEvent 时扫描所有 Bean 上的 @MicroFunc 方法并注册到 MicroFuncRegistry，
 * 为 MCP 协议层提供 tools/list 与 tools/call 能力。
 */
@Slf4j
@Service
public class MicroFuncService {

    // JSON 序列化/反序列化工具
    private static final Gson gson = new Gson();

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private MicroFuncRegistry registry;

    /**
     * 启动扫描：遍历所有 Spring Bean，将标注 @MicroFunc 的方法注册到容器
     */
    @EventListener(ApplicationReadyEvent.class)
    public void scanMicroFunctions() {
        //---- 1. 遍历所有 Bean ----
        String[] beanNames = applicationContext.getBeanDefinitionNames();
        int count = 0;

        for (String beanName : beanNames) {
            Object bean = applicationContext.getBean(beanName);
            Class<?> clazz = bean.getClass();

            //---- 2. 跳过 Spring 代理与 JDK 内部类 ----
            if (clazz.getName().contains("$$")) {
                continue;
            }
            if (clazz.getName().startsWith("java.") || clazz.getName().startsWith("org.springframework.")) {
                continue;
            }

            //---- 3. 扫描 @MicroFunc 方法 ----
            for (Method method : clazz.getDeclaredMethods()) {
                MicroFunc anno = method.getAnnotation(MicroFunc.class);
                if (anno == null) {
                    continue;
                }
                if (!Modifier.isPublic(method.getModifiers())) {
                    log.warn("[MicroFunc] 忽略非 public 方法: {}.{}", clazz.getName(), method.getName());
                    continue;
                }

                MicroFuncDefinition def = new MicroFuncDefinition(
                        anno.code(), anno.name(), anno.description(), bean, method);
                registry.register(def);
                count++;
            }
        }

        log.info("[MicroFunc] 扫描完成，共注册 {} 个微函数，总计 {} 个", count, registry.size());
    }

    /**
     * 构建 tools/list 响应：列出所有已注册微函数及其入参 Schema
     *
     * @return tools/list 标准响应
     */
    public ToolsListVo buildToolsList() {
        ToolsListVo vo = new ToolsListVo();
        List<ToolsListVo.Tool> tools = new ArrayList<>();

        for (MicroFuncDefinition def : registry.getAll()) {
            ToolsListVo.Tool tool = new ToolsListVo.Tool();
            tool.setName(def.getCode());
            tool.setDescription(def.getDescription());
            tool.setInputSchema(buildInputSchema(def));
            tools.add(tool);
        }

        vo.setTools(tools);
        return vo;
    }

    /**
     * 执行 tools/call：查找微函数 → JSON 反序列化注入 Dto → 反射调用
     *
     * @param name      微函数 code
     * @param arguments 调用参数 Map
     * @return tools/call 标准响应
     */
    public ToolsCallVo call(String name, Map<String, Object> arguments) {
        MicroFuncDefinition def = registry.get(name);
        if (def == null) {
            return buildErrorResult("微函数不存在: " + name);
        }

        try {
            //---- 1. DTO 注入与调用 ----
            Object result = null;
            int paramCount = def.getParameterTypes().length;

            if (paramCount == 0) {
                result = def.invoke();
            }
            if (paramCount == 1) {
                Object dto = deserializeDto(arguments, def.getParameterTypes()[0]);
                result = def.invoke(dto);
            }
            if (paramCount > 1) {
                Object[] dtoArgs = resolveMultiParams(def, arguments);
                result = def.invoke(dtoArgs);
            }

            //---- 2. 构建成功响应 ----
            ToolsCallVo vo = new ToolsCallVo();
            vo.setIsError(false);

            ToolsCallVo.Content content = new ToolsCallVo.Content();
            content.setType("text");
            content.setText(result != null ? gson.toJson(result) : "null");
            vo.setContent(Collections.singletonList(content));
            return vo;
        } catch (Exception e) {
            log.error("[MicroFunc] 调用微函数失败: code={} error={}", name, e.getMessage(), e);
            return buildErrorResult("调用失败: " + e.getMessage());
        }
    }

    // ==================== 辅助方法（满足方法抽取四问中的条件②：屏蔽第三方 API quirk） ====================

    /**
     * 将 Map 参数 JSON 反序列化为目标 Dto 类型（屏蔽 Gson API quirk）
     *
     * @param arguments  调用参数 Map
     * @param targetType 目标 Dto 类型
     * @return 反序列化后的 Dto 实例
     */
    private Object deserializeDto(Map<String, Object> arguments, Class<?> targetType) {
        if (arguments == null || arguments.isEmpty()) {
            return gson.fromJson("{}", targetType);
        }
        return gson.fromJson(gson.toJson(arguments), targetType);
    }

    /**
     * 多参数场景：按参数名匹配 Map 值并逐一反序列化
     *
     * @param def       微函数定义
     * @param arguments 调用参数 Map
     * @return 已注入的参数数组
     */
    private Object[] resolveMultiParams(MicroFuncDefinition def, Map<String, Object> arguments) {
        Class<?>[] types = def.getParameterTypes();
        Object[] args = new Object[types.length];
        java.lang.reflect.Parameter[] params = def.getMethod().getParameters();

        for (int i = 0; i < types.length; i++) {
            String paramName = params[i].getName();
            Object argValue = (arguments != null) ? arguments.get(paramName) : null;

            if (argValue == null) {
                args[i] = gson.fromJson("{}", types[i]);
                continue;
            }
            if (types[i].isInstance(argValue)) {
                args[i] = argValue;
                continue;
            }
            args[i] = gson.fromJson(gson.toJson(argValue), types[i]);
        }

        return args;
    }

    /**
     * 为单个微函数构建 JSON Schema 入参描述
     */
    private Map<String, Object> buildInputSchema(MicroFuncDefinition def) {
        Map<String, Object> schema = new LinkedHashMap<>();
        schema.put("type", "object");

        Map<String, Object> properties = new LinkedHashMap<>();
        List<String> required = new ArrayList<>();

        for (int i = 0; i < def.getParameterTypes().length; i++) {
            Class<?> paramType = def.getParameterTypes()[i];
            java.lang.reflect.Parameter param = def.getMethod().getParameters()[i];
            String paramName = param.getName();

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

    /**
     * 将 Java 类型递归解析为 JSON Schema 片段（仅构建 buildInputSchema 时调用）
     */
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

        return resolveDtoFieldsToSchema(type);
    }

    /**
     * 反射 Dto 字段生成 JSON Schema 对象属性描述
     */
    private Map<String, Object> resolveDtoFieldsToSchema(Class<?> dtoType) {
        Map<String, Object> schema = new LinkedHashMap<>();
        schema.put("type", "object");

        Map<String, Object> properties = new LinkedHashMap<>();
        for (Field field : dtoType.getDeclaredFields()) {
            if (Modifier.isStatic(field.getModifiers())) {
                continue;
            }
            Map<String, Object> fieldProp = resolveFieldToSchema(field);
            properties.put(field.getName(), fieldProp);
        }

        schema.put("properties", properties);
        return schema;
    }

    /**
     * 解析单字段类型 Schema（含 List 泛型内嵌类型）
     */
    private Map<String, Object> resolveFieldToSchema(Field field) {
        Class<?> fieldType = field.getType();

        if (fieldType == List.class) {
            Map<String, Object> prop = new LinkedHashMap<>();
            prop.put("type", "array");
            Type genericType = field.getGenericType();
            if (genericType instanceof ParameterizedType pt) {
                Type itemType = pt.getActualTypeArguments()[0];
                if (itemType instanceof Class<?> itemClass) {
                    prop.put("items", resolveTypeToSchema(itemClass));
                }
            }
            return prop;
        }

        return resolveTypeToSchema(fieldType);
    }

    /**
     * 构建失败调用结果（满足条件①：被 call() 中两处错误路径复用）
     */
    private ToolsCallVo buildErrorResult(String errorMsg) {
        ToolsCallVo vo = new ToolsCallVo();
        vo.setIsError(true);

        ToolsCallVo.Content content = new ToolsCallVo.Content();
        content.setType("text");
        content.setText(errorMsg);
        vo.setContent(Collections.singletonList(content));
        return vo;
    }
}
