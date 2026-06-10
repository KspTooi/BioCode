package com.ksptool.bio.biz.aacp.commons;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 微函数注册容器：持有所有 @MicroFunc 运行时定义（对应 QT 的 QMetaObject）
 * <p>
 * 职责仅限于注册与按 target 查询，不含任何业务逻辑（Schema 生成、DTO 注入、调用执行均由 MicroFuncService 负责）。
 * 线程安全，供 MCP 协议层并发查询。
 */
@Slf4j
@Component
public class MicroFuncRegistry {

    // target → MicroFuncDefinition 映射，线程安全
    private final Map<String, MicroFuncDefinition> registry = new ConcurrentHashMap<>();

    /**
     * 注册单个微函数定义，同名覆盖时打 warn 日志
     *
     * @param def 微函数运行时定义
     */
    public void register(MicroFuncDefinition def) {
        if (def == null) {
            return;
        }
        MicroFuncDefinition existed = registry.put(def.getTarget(), def);
        if (existed != null) {
            log.warn("[MicroFunc] 微函数 {} 被覆盖: {} -> {}", def.getTarget(), existed.getName(), def.getName());
            return;
        }
        log.info("[MicroFunc] 注册微函数: target={} name={} params={}", def.getTarget(), def.getName(), def.getParameterTypes().length);
    }

    /**
     * 批量注册
     *
     * @param defs 微函数定义列表
     */
    public void registerAll(List<MicroFuncDefinition> defs) {
        if (defs == null || defs.isEmpty()) {
            return;
        }
        for (MicroFuncDefinition def : defs) {
            register(def);
        }
    }

    /**
     * 按 target 查找
     *
     * @param target 微函数标识
     * @return 微函数定义，不存在返回 null
     */
    public MicroFuncDefinition get(String target) {
        if (StringUtils.isBlank(target)) {
            return null;
        }
        return registry.get(target);
    }

    /**
     * 获取全部已注册微函数（只读视图）
     *
     * @return 不可修改的集合
     */
    public Collection<MicroFuncDefinition> getAll() {
        return Collections.unmodifiableCollection(registry.values());
    }

    /**
     * 已注册数量
     *
     * @return 容器大小
     */
    public int size() {
        return registry.size();
    }
}
