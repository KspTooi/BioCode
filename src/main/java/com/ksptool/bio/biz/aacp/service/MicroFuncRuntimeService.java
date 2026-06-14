package com.ksptool.bio.biz.aacp.service;

import com.google.gson.Gson;
import com.ksptool.bio.biz.aacp.commons.MicroFuncDef;
import com.ksptool.bio.biz.aacp.commons.MicroFuncParamResolver;
import com.ksptool.bio.biz.aacp.commons.annotation.MicroFunc;
import com.ksptool.bio.biz.aacp.commons.annotation.Param;
import com.ksptool.bio.biz.aacp.commons.jrpc.vo.ToolsCallVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 微函数运行时服务：启动扫描 + 注册容器 + 调用引擎。
 * <p>
 * 整合了原 MicroFuncRegistry 和 MicroFuncCallService 的职责。
 */
@Slf4j
@Service
public class MicroFuncRuntimeService {

    private final Map<String, MicroFuncDef> registry = new ConcurrentHashMap<>();

    @Autowired
    private ApplicationContext applicationContext;

    /**
     * 启动时扫描所有 Bean 上的 @MicroFunc 方法并注册
     */
    @EventListener(ApplicationReadyEvent.class)
    public void init() {
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

                Parameter[] params = method.getParameters();
                for (int i = 0; i < params.length; i++) {
                    if (params[i].getAnnotation(Param.class) == null) {
                        log.error("[MicroFunc] 拒绝注册: {}.{} 的第 {} 个参数 (类型 {}) 缺少 @Param 注解",
                                clazz.getName(), method.getName(), i + 1, params[i].getType().getName());
                        continue methodLoop;
                    }
                }

                MicroFuncDef def = MicroFuncDef.of(anno.target(), anno.name(), anno.description(), bean, method);
                register(def);
                count++;
            }
        }

        log.info("[MicroFunc] 扫描完成，共注册 {} 个微函数，总计 {} 个", count, registry.size());
    }

    /**
     * 注册微函数定义，同名覆盖时打 warn
     */
    public void register(MicroFuncDef def) {
        if (def == null) {
            return;
        }
        MicroFuncDef existed = registry.put(def.getTarget(), def);
        if (existed != null) {
            log.warn("[MicroFunc] 微函数 {} 被覆盖: {} -> {}", def.getTarget(), existed.getName(), def.getName());
            return;
        }
        log.info("[MicroFunc] 注册微函数: target={} name={} params={}", def.getTarget(), def.getName(), def.getParameters().length);
    }

    public void remove(String target) {
        registry.remove(target);
    }

    public MicroFuncDef get(String target) {
        if (StringUtils.isBlank(target)) {
            return null;
        }
        return registry.get(target);
    }

    public Collection<MicroFuncDef> getAll() {
        return Collections.unmodifiableCollection(registry.values());
    }

    public int size() {
        return registry.size();
    }

    /**
     * 调用微函数：根据 target 从注册表查找定义，解析参数并反射调用。
     *
     * @param target    微函数 target
     * @param arguments 输入参数 Map
     * @return MCP tools/call 响应
     */
    public ToolsCallVo call(String target, Map<String, Object> arguments) {
        MicroFuncDef def = registry.get(target);
        if (def == null) {
            ToolsCallVo errVo = new ToolsCallVo();
            errVo.setIsError(true);
            ToolsCallVo.Content errContent = new ToolsCallVo.Content();
            errContent.setType("text");
            errContent.setText("微函数未注册: " + target);
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
            log.error("[MicroFunc] 调用微函数失败: target={} error={}", target, e.getMessage(), e);
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