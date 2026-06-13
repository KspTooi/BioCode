package com.ksptool.bio.biz.aacp.service;

import com.ksptool.bio.biz.aacp.commons.MicroFuncDef;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class MicroFuncRuntimeService {

    //微函数定义
    private final Map<String, MicroFuncDef> registry = new ConcurrentHashMap<>();

    @EventListener(ApplicationReadyEvent.class)
    public void init() {

    }

    /**
     * 注册微函数定义
     *
     * @param definition 微函数定义
     */
    public void register(MicroFuncDef definition) {

    }

    /**
     * 移除微函数定义
     *
     * @param target 微函数唯一标识
     */
    public void remove(String target) {
        registry.remove(target);
    }

    /**
     * 获取微函数定义
     *
     * @param target 微函数唯一标识
     * @return 微函数定义
     */
    public MicroFuncDef get(String target) {
        return registry.get(target);
    }

    /**
     * 获取所有微函数定义
     *
     * @return 微函数定义列表
     */
    public List<MicroFuncDef> getAll() {
        return new ArrayList<>(registry.values());
    }

}
