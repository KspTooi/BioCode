package com.ksptool.bio.biz.qf.commons.listener;

import com.ksptool.bio.biz.qf.commons.QfVarsProc;
import com.ksptool.bio.biz.qf.commons.event.QfProcFinishedEvent;
import com.ksptool.bio.biz.qf.model.qftodo.QfTodoPo;
import com.ksptool.bio.biz.qf.repository.QfTodoRepository;
import lombok.extern.slf4j.Slf4j;
import org.flowable.common.engine.api.delegate.event.FlowableEngineEntityEvent;
import org.flowable.common.engine.api.delegate.event.FlowableEngineEventType;
import org.flowable.engine.delegate.event.AbstractFlowableEngineEventListener;
import org.flowable.engine.runtime.ProcessInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
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
    @Lazy
    @Autowired
    private ApplicationEventPublisher aep;
    public QfProcFinishedListener() {
        super(Set.of(FlowableEngineEventType.PROCESS_COMPLETED));
    }

    @Override
    protected void processCompleted(FlowableEngineEntityEvent event) {
        ProcessInstance pi = (ProcessInstance) event.getEntity();
        String procId = pi.getId();

        //从流程变量获取业务数据
        Map<String, Object> vars = pi.getProcessVariables();
        Long bizFormId = vars.get(QfVarsProc.BIZ_FORM_ID.toString()) != null
                ? Long.valueOf(vars.get(QfVarsProc.BIZ_FORM_ID.toString()).toString())
                : null;
        Long dataId = vars.get(QfVarsProc.DATA_ID.toString()) != null
                ? Long.valueOf(vars.get(QfVarsProc.DATA_ID.toString()).toString())
                : null;

        //处理残留待办
        List<QfTodoPo> remaining = qfTodoRepository.findAllByEngProcIdAndStatus(procId, 0);
        for (QfTodoPo po : remaining) {
            po.setStatus(10);
            qfTodoRepository.save(po);
        }

        //无论是否有残留待办，都发布流程结束事件
        if (bizFormId != null && dataId != null) {
            //从流程变量获取审批结果（排他网关实际使用的判断依据）
            // approved=true 同意结束, approved=false 驳回结束
            Boolean approved = vars.get("approved") != null
                    ? Boolean.valueOf(vars.get("approved").toString())
                    : null;
            Integer action = approved != null ? (approved ? 0 : 1) : 0;

            QfProcFinishedEvent fireEvent = new QfProcFinishedEvent(this);
            fireEvent.setBizFormId(bizFormId);
            fireEvent.setDataId(dataId);
            fireEvent.setAction(action);
            aep.publishEvent(fireEvent);
            log.info("[QfProcFinishedListener] 流程结束事件已发布, bizFormId: {}, dataId: {}, approved: {}", bizFormId, dataId, approved);
        }

        if (!remaining.isEmpty()) {
            log.info("[QfProcFinishedListener] 流程结束，共作废 {} 条残留待办, procId: {}", remaining.size(), procId);
        }
    }
}
