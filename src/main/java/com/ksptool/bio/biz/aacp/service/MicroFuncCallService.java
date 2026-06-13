package com.ksptool.bio.biz.aacp.service;

import com.google.gson.Gson;
import com.ksptool.bio.biz.aacp.commons.MicroFuncDefinition;
import com.ksptool.bio.biz.aacp.commons.MicroFuncRegistry;
import com.ksptool.bio.biz.aacp.commons.annotation.MicroFunc;
import com.ksptool.bio.biz.aacp.commons.jrpc.vo.ToolsCallVo;
import com.ksptool.bio.biz.aacp.model.func.AacpFuncPo;
import com.ksptool.bio.biz.aacp.repository.MicroFuncRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.Map;

/**
 * 微函数调用引擎：启动扫描 + DTO 注入调用
 * <p>
 * 在 ApplicationReadyEvent 时扫描所有 Bean 上的 @MicroFunc 方法并注册到 MicroFuncRegistry，
 * 为 MCP 协议层提供 tools/list 与 tools/call 能力。
 */
@Slf4j
@Service
public class MicroFuncCallService {

    private static final Gson gson = new Gson();

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private MicroFuncRegistry registry;

    @Autowired
    private MicroFuncRepository microFuncRepository;

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
                        anno.target(), anno.name(), anno.description(), bean, method);
                registry.register(def);
                count++;
            }
        }

        log.info("[MicroFunc] 扫描完成，共注册 {} 个微函数，总计 {} 个", count, registry.size());
    }

    public ToolsCallVo call(String name, Map<String, Object> arguments) {
        AacpFuncPo funcPo = microFuncRepository.getByCode(name);
        if (funcPo == null) {
            ToolsCallVo errVo = new ToolsCallVo();
            errVo.setIsError(true);
            ToolsCallVo.Content errContent = new ToolsCallVo.Content();
            errContent.setType("text");
            errContent.setText("微函数不存在: " + name);
            errVo.setContent(Collections.singletonList(errContent));
            return errVo;
        }
        MicroFuncDefinition def = registry.get(funcPo.getTarget());
        if (def == null) {
            ToolsCallVo errVo = new ToolsCallVo();
            errVo.setIsError(true);
            ToolsCallVo.Content errContent = new ToolsCallVo.Content();
            errContent.setType("text");
            errContent.setText("微函数未注册: " + funcPo.getTarget());
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
                Class<?> paramType = def.getParameterTypes()[0];
                String paramName = def.getMethod().getParameters()[0].getName();
                Object argValue = (arguments != null) ? arguments.get(paramName) : null;
                if (argValue == null) {
                    result = def.invoke(gson.fromJson("{}", paramType));
                } else if (paramType.isInstance(argValue)) {
                    result = def.invoke(argValue);
                } else {
                    result = def.invoke(gson.fromJson(gson.toJson(argValue), paramType));
                }
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
}
