package com.ksptool.bio.biz.qf.commons.config;

import com.ksptool.bio.biz.qf.commons.listener.QfTaskCreatedListener;
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
 */
@Configuration
public class QfFlowableConfig implements EngineConfigurationConfigurer<SpringProcessEngineConfiguration> {

    @Autowired
    private QfTaskCreatedListener qfTaskCreatedListener;

    @Override
    public void configure(SpringProcessEngineConfiguration cfg) {

        //先创建一个空的Map，用来存放事件类型和监听器列表
        var typedListeners = new HashMap<String, List<FlowableEventListener>>();

        //再创建一个空的List，用来存放监听器
        var listeners = new ArrayList<FlowableEventListener>();

        //获取引擎里面现有的TASK_CREATED监听器
        var existTypes = cfg.getTypedEventListeners();

        if (existTypes != null) {

            var existCreatedListeners = existTypes.get(FlowableEngineEventType.TASK_CREATED.name());

            //如果引擎里面已有TASK_CREATED监听器，则添加到列表中
            if (existCreatedListeners != null) {
                listeners.addAll(existCreatedListeners);
            }

        }

        //将注入的监听器添加到列表中
        listeners.add(qfTaskCreatedListener);

        //将新的列表放回 Map，并更新引擎配置
        typedListeners.put(FlowableEngineEventType.TASK_CREATED.name(), listeners);
        cfg.setTypedEventListeners(typedListeners);
    }
}