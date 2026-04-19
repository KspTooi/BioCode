package com.ksptool.bio.biz.qf.commons.listener;

import com.ksptool.bio.biz.qf.repository.QfTodoRepository;
import lombok.extern.slf4j.Slf4j;
import org.flowable.common.engine.api.delegate.event.FlowableEngineEntityEvent;
import org.flowable.common.engine.api.delegate.event.FlowableEngineEventType;
import org.flowable.engine.delegate.event.AbstractFlowableEngineEventListener;
import org.flowable.task.api.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * 任务完成监听器
 * <p>
 * 监听引擎 TASK_COMPLETED 事件，将对应的 QfTodoPo 状态更新为已办(status=1)。
 * 注意：审批服务 (QfTodoService.approveQfTodo) 在完成引擎任务前已更新待办状态，
 * 本监听器作为兜底，处理通过引擎直接完成任务（不经过审批服务）的场景。
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
public class QfTaskFinishedListener extends AbstractFlowableEngineEventListener {

    @Lazy
    @Autowired
    private QfTodoRepository qfTodoRepository;

    public QfTaskFinishedListener() {
        super(Set.of(FlowableEngineEventType.TASK_COMPLETED));
    }

    @Override
    protected void taskCompleted(FlowableEngineEntityEvent event) {
        Task task = (Task) event.getEntity();
        var po = qfTodoRepository.findByEngTaskId(task.getId());

        if (po == null) {
            log.warn("[QfTaskFinishedListener] 未找到对应待办记录, 引擎任务ID: {}", task.getId());
            return;
        }

        if (po.getStatus() != 0) {
            return;
        }

        po.setStatus(1);
        po.setFinTime(LocalDateTime.now());
        qfTodoRepository.save(po);
        log.debug("[QfTaskFinishedListener] 待办已标记为已办, todoId: {}", po.getId());
    }
}
