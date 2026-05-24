package com.ksptool.bio.biz.qf.commons.config;

import com.ksptool.bio.biz.qf.commons.QfSnowflakeIdGenerator;
import com.ksptool.bio.biz.qf.commons.listener.QfMiRenameParseHandler;
import com.ksptool.bio.biz.qf.commons.listener.QfProcFinishedListener;
import com.ksptool.bio.biz.qf.commons.listener.QfTaskAssignedListener;
import com.ksptool.bio.biz.qf.commons.listener.QfTaskCancelledListener;
import com.ksptool.bio.biz.qf.commons.listener.QfTaskCreatedListener;
import com.ksptool.bio.biz.qf.commons.listener.QfTaskFinishedListener;
import org.flowable.common.engine.api.delegate.event.FlowableEngineEventType;
import org.flowable.common.engine.api.delegate.event.FlowableEventListener;
import org.flowable.spring.SpringProcessEngineConfiguration;
import org.flowable.spring.boot.EngineConfigurationConfigurer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Flowable 引擎自定义配置 (QF域)
 * 这个配置的作用是:
 * <p>
 * 用来注册Flowable引擎的的监听器
 *
 * <p>
 * Flowable引擎有许多事件，每个事件可以有多个监听器。
 * 比如TASK_CREATED事件，可以有多个监听器。
 * 可以通过FlowableEngineEventType枚举类来获取事件类型。
 *
 * @author WangQingHua(603484930@qq.com)
 * @author Akkarin(1075613357@qq.com)
 * @author (Ish)Yuumi(1144150092@qq.com)
 * @license Proprietary
 * 版权所有 (c) 2026 KspTool及其贡献者保留所有权利。
 * 未经事先书面许可，严禁任何形式的复制或分发。
 * @since 2026-04-15
 */
@Configuration
public class QfFlowableConfig implements EngineConfigurationConfigurer<SpringProcessEngineConfiguration> {

    @Autowired
    private QfTaskCreatedListener qfTaskCreatedListener;

    @Autowired
    private QfTaskFinishedListener qfTaskFinishedListener;

    @Autowired
    private QfTaskCancelledListener qfTaskCancelledListener;

    @Autowired
    private QfProcFinishedListener qfProcFinishedListener;

    @Autowired
    private QfTaskAssignedListener qfTaskAssignedListener;

    @Override
    public void configure(SpringProcessEngineConfiguration cfg) {

        var typedListeners = new HashMap<String, List<FlowableEventListener>>();
        var existTypes = cfg.getTypedEventListeners();

        //任务创建监听器
        typedListeners.put(
                FlowableEngineEventType.TASK_CREATED.name(),
                mergeListeners(existTypes, FlowableEngineEventType.TASK_CREATED, qfTaskCreatedListener)
        );

        //任务分配监听器
        typedListeners.put(
                FlowableEngineEventType.TASK_ASSIGNED.name(),
                mergeListeners(existTypes, FlowableEngineEventType.TASK_ASSIGNED, qfTaskAssignedListener)
        );

        //任务完成监听器
        typedListeners.put(
                FlowableEngineEventType.TASK_COMPLETED.name(),
                mergeListeners(existTypes, FlowableEngineEventType.TASK_COMPLETED, qfTaskFinishedListener)
        );

        //任务取消监听器
        typedListeners.put(
                FlowableEngineEventType.ACTIVITY_CANCELLED.name(),
                mergeListeners(existTypes, FlowableEngineEventType.ACTIVITY_CANCELLED, qfTaskCancelledListener)
        );

        //流程结束监听器
        typedListeners.put(
                FlowableEngineEventType.PROCESS_COMPLETED.name(),
                mergeListeners(existTypes, FlowableEngineEventType.PROCESS_COMPLETED, qfProcFinishedListener)
        );

        cfg.setTypedEventListeners(typedListeners);

        //注册多实例变量重命名处理器，在部署期将 ${assigneeList}/${groupList} 改写为 ${qfMi_<taskId>}
        var preHandlers = cfg.getPreBpmnParseHandlers();
        if (preHandlers == null) {
            preHandlers = new ArrayList<>();
            cfg.setPreBpmnParseHandlers(preHandlers);
        }
        preHandlers.add(new QfMiRenameParseHandler());

        //注册雪花算法ID生成器
        cfg.setIdGenerator(new QfSnowflakeIdGenerator());
    }

    /**
     * 合并引擎已有监听器与新监听器，避免覆盖引擎内置监听
     */
    private List<FlowableEventListener> mergeListeners(
            java.util.Map<String, List<FlowableEventListener>> existTypes,
            FlowableEngineEventType type,
            FlowableEventListener newListener) {

        var list = new ArrayList<FlowableEventListener>();

        if (existTypes != null) {
            var existing = existTypes.get(type.name());
            if (existing != null) {
                list.addAll(existing);
            }
        }

        list.add(newListener);
        return list;
    }
}
