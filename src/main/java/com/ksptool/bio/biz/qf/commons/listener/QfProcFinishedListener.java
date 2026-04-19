package com.ksptool.bio.biz.qf.commons.listener;

import com.ksptool.bio.biz.qf.model.qftodo.QfTodoPo;
import com.ksptool.bio.biz.qf.repository.QfTodoRepository;
import lombok.extern.slf4j.Slf4j;
import org.flowable.common.engine.api.delegate.event.FlowableEngineEntityEvent;
import org.flowable.common.engine.api.delegate.event.FlowableEngineEventType;
import org.flowable.engine.delegate.event.AbstractFlowableEngineEventListener;
import org.flowable.engine.runtime.ProcessInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

/**
 * 流程结束监听器
 * <p>
 * 监听引擎 PROCESS_COMPLETED 事件，将该流程实例下所有仍处于待办状态(status=0)的
 * QfTodoPo 批量标记为已作废(status=10)，防止残留孤儿待办。
 * <p>
 * 正常审批路径下，待办在 TASK_COMPLETED / ACTIVITY_CANCELLED 时已被更新；
 * 本监听器作为最终兜底，覆盖异常终止、管理员强制结束等边缘场景。
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
public class QfProcFinishedListener extends AbstractFlowableEngineEventListener {

    @Lazy
    @Autowired
    private QfTodoRepository qfTodoRepository;

    public QfProcFinishedListener() {
        super(Set.of(FlowableEngineEventType.PROCESS_COMPLETED));
    }

    @Override
    protected void processCompleted(FlowableEngineEntityEvent event) {
        ProcessInstance pi = (ProcessInstance) event.getEntity();
        String procId = pi.getId();

        List<QfTodoPo> remaining = qfTodoRepository.findAllByEngProcIdAndStatus(procId, 0);

        if (remaining.isEmpty()) {
            return;
        }

        for (QfTodoPo po : remaining) {
            po.setStatus(10);
            qfTodoRepository.save(po);
        }

        log.debug("[QfProcFinishedListener] 流程结束，共作废 {} 条残留待办, procId: {}", remaining.size(), procId);
    }
}
