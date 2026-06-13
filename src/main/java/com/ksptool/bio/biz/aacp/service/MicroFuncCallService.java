package com.ksptool.bio.biz.aacp.service;

import com.google.gson.Gson;
import com.ksptool.bio.biz.aacp.commons.MicroFuncDefinition;
import com.ksptool.bio.biz.aacp.commons.MicroFuncParamResolver;
import com.ksptool.bio.biz.aacp.commons.MicroFuncRegistry;
import com.ksptool.bio.biz.aacp.commons.annotation.MicroFunc;
import com.ksptool.bio.biz.aacp.commons.annotation.Param;
import com.ksptool.bio.biz.aacp.commons.jrpc.vo.ToolsCallVo;
import com.ksptool.bio.biz.aacp.model.func.AacpMicroFuncPo;
import com.ksptool.bio.biz.aacp.repository.MicroFuncRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
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

            methodLoop:
            for (Method method : clazz.getDeclaredMethods()) {
                MicroFunc anno = method.getAnnotation(MicroFunc.class);
                if (anno == null) {
                    continue;
                }
                if (!Modifier.isPublic(method.getModifiers())) {
                    log.warn("[MicroFunc] 忽略非 public 方法: {}.{}", clazz.getName(), method.getName());
                    continue;
                }

                //严格校验：每个参数必须标注 @Param
                java.lang.reflect.Parameter[] params = method.getParameters();
                for (int i = 0; i < params.length; i++) {
                    if (params[i].getAnnotation(Param.class) == null) {
                        log.error("[MicroFunc] 拒绝注册: {}.{} 的第 {} 个参数 (类型 {}) 缺少 @Param 注解",
                                clazz.getName(), method.getName(), i + 1, params[i].getType().getName());
                        continue methodLoop;
                    }
                }

                MicroFuncDefinition def = MicroFuncDefinition.of(
                        anno.target(), anno.name(), anno.description(), bean, method);
                registry.register(def);
                count++;
            }
        }

        log.info("[MicroFunc] 扫描完成，共注册 {} 个微函数，总计 {} 个", count, registry.size());
    }

    public ToolsCallVo call(String name, Map<String, Object> arguments) {
        AacpMicroFuncPo funcPo = microFuncRepository.getByCode(name);
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
            var resolver = MicroFuncParamResolver.of(arguments, Arrays.asList(def.getParameters()));
            Object result = def.invoke(resolver.resolve());

            ToolsCallVo vo = new ToolsCallVo();
            vo.setIsError(false);
            ToolsCallVo.Content content = new ToolsCallVo.Content();
            content.setType("text");
            content.setText(result != null ? new Gson().toJson(result) : "null");
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
