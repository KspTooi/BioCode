package com.ksptool.bio.biz.qt.common;

import lombok.extern.slf4j.Slf4j;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * QF任务注册表
 * 
 * @author KspTool(ksptool@outlook.com)
 * @since 2026-02-10
 * @license Apache License 2.0
 */
@Slf4j
@Component
public class QuickTaskRegistry implements ApplicationListener<ApplicationReadyEvent> {

    // 核心容器：BeanName -> Bean实例
    private static final Map<String, QuickTask> QT_BEAN_MAP = new ConcurrentHashMap<>();

    @Autowired
    private Scheduler scheduler;

    /**
     * 提供给 Quartz Job 调用
     */
    public static QuickTask<?> getBean(String name) {
        return QT_BEAN_MAP.get(name);
    }

    /**
     * 判断是否包含本地任务Bean
     *
     * @param name Bean名称
     * @return 是否包含
     */
    public static boolean contains(String name) {
        return QT_BEAN_MAP.containsKey(name);
    }

    /**
     * 获取所有本地任务Bean
     *
     * @return 本地任务Bean列表
     */
    public static ArrayList<QuickTask> getBeans() {
        Collection<QuickTask> values = QT_BEAN_MAP.values();
        return new ArrayList<>(values);
    }

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {

        Map<String, QuickTask> beans = event.getApplicationContext().getBeansOfType(QuickTask.class);

        for (Map.Entry<String, QuickTask> entry : beans.entrySet()) {

            var bean = entry.getValue();
            var simpleName = bean.getClass().getSimpleName();

            //如果出现冲突则抛出异常
            if (QT_BEAN_MAP.containsKey(simpleName)) {
                throw new RuntimeException("QuickTask初始化时产生了一个冲突: " + simpleName + "请检查是否存在重复的本地任务Bean");
            }

            QT_BEAN_MAP.put(simpleName, bean);
            log.info("QuickTask 加载本地任务: {}", simpleName);

        }

        //确认 Map 准备好后，手动启动 Quartz
        try {
            log.info("QuickTask 注册完成,共有 {} 个本地任务Bean", QT_BEAN_MAP.size());
            scheduler.start();
            log.info("QuickTask 已启动 QuartZ 调度器。");
        } catch (SchedulerException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException("QuickTask 启动 QuartZ 调度器时发生了一个错误.");
        }

    }
}
