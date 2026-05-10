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
 * 监听引擎 ACTIVITY_CANCELLED 事件，过滤 UserTask 类型的节点取消。
 * 通过 executionId 查询对应的引擎任务实例，再精确作废对应的 QfTodoPo(status=10)。
 * <p>
 * 典型触发场景：边界事件打断任务、管理员驳回后引擎回溯取消上游任务等。
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
            var po = qfTodoRepository.findByEngTaskId(task.getId());
            if (po == null) {
                continue;
            }
            if (po.getStatus() != 0) {
                continue;
            }
            po.setStatus(10);
            qfTodoRepository.save(po);
            //发布任务取消事件
            QfTaskCancelledEvent fireEvent = new QfTaskCancelledEvent(this);
            assign(po, fireEvent);
            fireEvent.setReason("流程取消");
            aep.publishEvent(fireEvent);
            log.debug("[QfTaskCancelledListener] 待办已标记为已作废, todoId: {}, taskId: {}", po.getId(), task.getId());
        }
    }
}
