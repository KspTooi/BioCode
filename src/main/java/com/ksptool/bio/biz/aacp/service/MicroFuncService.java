package com.ksptool.bio.biz.aacp.service;

import com.google.gson.Gson;
import com.ksptool.bio.biz.aacp.commons.MicroFuncDefinition;
import com.ksptool.bio.biz.aacp.commons.MicroFuncRegistry;
import com.ksptool.bio.biz.aacp.commons.annotation.MicroFunc;
import com.ksptool.bio.biz.aacp.commons.jrpc.vo.ToolsCallVo;
import com.ksptool.bio.biz.aacp.commons.jrpc.vo.ToolsListVo;
import com.ksptool.bio.biz.aacp.model.AacpFuncPo;
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

    //JSON序列化/反序列化工具
    private static final Gson gson = new Gson();

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private MicroFuncRegistry registry;

    @EventListener(ApplicationReadyEvent.class)
    public void scanMicroFunctions() {
        String[] beanNames = applicationContext.getBeanDefinitionNames();
        int count = 0;

        for (String beanName : beanNames) {
            Object bean = applicationContext.getBean(beanName);
            Class<?> clazz = bean.getClass();

            if (clazz.getName().contains("$$")) {
                clazz = clazz.getSuperclass();
                if (clazz == null || clazz == Object.class) {
                    continue;
                }
            }
            if (clazz.getName().startsWith("java.") || clazz.getName().startsWith("org.springframework.")) {
                continue;
            }

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

    public ToolsListVo buildToolsList() {
        ToolsListVo vo = new ToolsListVo();
        List<ToolsListVo.Tool> tools = new ArrayList<>();

        for (MicroFuncDefinition def : registry.getAll()) {
            ToolsListVo.Tool tool = new ToolsListVo.Tool();
            tool.setName(def.getCode());
            tool.setDescription(def.getDescription());

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
            tool.setInputSchema(schema);
            tools.add(tool);
        }

        vo.setTools(tools);
        return vo;
    }

    /**
     * 根据数据库中的微函数列表构建工具列表（仅返回注册表中存在的 code）
     */
    public ToolsListVo buildToolsListByFuncs(List<AacpFuncPo> funcs) {
        ToolsListVo vo = new ToolsListVo();
        List<ToolsListVo.Tool> tools = new ArrayList<>();
        if (funcs == null || funcs.isEmpty()) {
            vo.setTools(tools);
            return vo;
        }

        for (AacpFuncPo fpo : funcs) {
            MicroFuncDefinition def = registry.get(fpo.getCode());
            if (def == null) {
                log.warn("[AACP] 微函数未注册: code={}", fpo.getCode());
                continue;
            }

            ToolsListVo.Tool tool = new ToolsListVo.Tool();
            tool.setName(def.getCode());
            tool.setDescription(def.getDescription());

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
            tool.setInputSchema(schema);
            tools.add(tool);
        }

        vo.setTools(tools);
        return vo;
    }

    public ToolsCallVo call(String name, Map<String, Object> arguments) {
        MicroFuncDefinition def = registry.get(name);
        if (def == null) {
            ToolsCallVo errVo = new ToolsCallVo();
            errVo.setIsError(true);
            ToolsCallVo.Content errContent = new ToolsCallVo.Content();
            errContent.setType("text");
            errContent.setText("微函数不存在: " + name);
            errVo.setContent(Collections.singletonList(errContent));
            return errVo;
        }

        try {
            Object result = null;
            int paramCount = def.getParameterTypes().length;

            if (paramCount == 0) {
                result = def.invoke();
            }
            if (paramCount == 1) {
                Object dto;
                if (arguments == null || arguments.isEmpty()) {
                    dto = gson.fromJson("{}", def.getParameterTypes()[0]);
                } else {
                    dto = gson.fromJson(gson.toJson(arguments), def.getParameterTypes()[0]);
                }
                result = def.invoke(dto);
            }
            if (paramCount > 1) {
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
                result = def.invoke(args);
            }

            ToolsCallVo vo = new ToolsCallVo();
            vo.setIsError(false);
            ToolsCallVo.Content content = new ToolsCallVo.Content();
            content.setType("text");
            content.setText(result != null ? gson.toJson(result) : "null");
            vo.setContent(Collections.singletonList(content));
            return vo;
        } catch (Exception e) {
            log.error("[MicroFunc] 调用微函数失败: code={} error={}", name, e.getMessage(), e);
            ToolsCallVo errVo = new ToolsCallVo();
            errVo.setIsError(true);
            ToolsCallVo.Content errContent = new ToolsCallVo.Content();
            errContent.setType("text");
            errContent.setText("调用失败: " + e.getMessage());
            errVo.setContent(Collections.singletonList(errContent));
            return errVo;
        }
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

        //反射Dto字段（原resolveDtoFieldsToSchema + resolveFieldToSchema内联）
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
