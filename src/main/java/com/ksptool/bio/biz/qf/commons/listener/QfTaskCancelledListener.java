package com.ksptool.bio.biz.qf.commons.listener;

import com.ksptool.bio.biz.qf.commons.event.QfTaskCancelledEvent;
import com.ksptool.bio.biz.qf.repository.QfTodoRepository;
import lombok.extern.slf4j.Slf4j;
import org.flowable.common.engine.api.delegate.event.FlowableEngineEventType;
import org.flowable.engine.TaskService;
import org.flowable.engine.delegate.event.AbstractFlowableEngineEventListener;
import org.flowable.engine.delegate.event.FlowableActivityCancelledEvent;
import org.flowable.task.api.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

import static com.ksptool.entities.Entities.assign;

/**
 * 任务取消监听器
 * <p>
 * 监听 ACTIVITY_CANCELLED 事件：
 * 边界事件打断任务、管理员驳回后引擎回溯取消上游任务、多实例或签完成后引擎取消其余并行实例。
 * </p>
 * <p>
 * 注意：Flowable 8.0.0 没有 TASK_CANCELLED 事件枚举值，无法通过 onEvent 分发。
 * 或签/并签场景下，任务完成时由 QfTodoService.cancelOrphanedTodos 主动作废同级待办。
 *
 * @author WangQingHua(603484930@qq.com)
 * @author KspTool(ksptool@outlook.com)
 * @since 2026-04-16
 * @license Proprietary
 * 版权所有 (c) 2026 KspTool及其贡献者保留所有权利。
 * 未经事先书面许可，严禁任何形式的复制或分发。
 */
@Slf4j
@Component
public class QfTaskCancelledListener extends AbstractFlowableEngineEventListener {

    @Lazy
    @Autowired
    private QfTodoRepository qfTodoRepository;

    @Lazy
    @Autowired
    private TaskService taskService;

    @Lazy
    @Autowired
    private ApplicationEventPublisher aep;

    public QfTaskCancelledListener() {
        super(Set.of(FlowableEngineEventType.ACTIVITY_CANCELLED));
    }

    @Override
    protected void activityCancelled(FlowableActivityCancelledEvent event) {
        if (!"userTask".equals(event.getActivityType())) {
            return;
        }

        // 通过 executionId 查询被取消的任务实例，获取其 engTaskId
        List<Task> tasks = taskService.createTaskQuery()
                .executionId(event.getExecutionId())
                .list();

        for (Task task : tasks) {
            cancelTodo(task.getId());
        }
    }

    /**
     * 根据引擎任务ID作废对应的待办
     */
    private void cancelTodo(String engTaskId) {
        var po = qfTodoRepository.findByEngTaskIdAndStatus(engTaskId, 0);
        if (po == null) {
            return;
        }
        if (po.getStatus() != 0) {
            return;
        }
        po.setStatus(10);
        qfTodoRepository.save(po);
        //发布任务取消事件
        QfTaskCancelledEvent fireEvent = new QfTaskCancelledEvent(this);
        assign(po, fireEvent);
        fireEvent.setReason("流程取消");
        aep.publishEvent(fireEvent);
        log.debug("[QfTaskCancelledListener] 待办已标记为已作废, todoId: {}, taskId: {}", po.getId(), engTaskId);
    }
}