package com.ksptool.bio.biz.qf.commons.listener;

import com.ksptool.bio.biz.qf.commons.QfMemberKinds;
import com.ksptool.bio.biz.qf.commons.QfProcTools;
import com.ksptool.bio.biz.qf.commons.QfVarsProc;
import com.ksptool.bio.biz.qf.commons.event.QfTaskAssignedEvent;
import com.ksptool.bio.biz.qf.commons.event.QfTaskStartedEvent;
import com.ksptool.bio.biz.qf.model.qftodo.QfTodoPo;
import com.ksptool.bio.biz.qf.repository.QfTodoRepository;
import com.ksptool.bio.biz.qf.service.QfMemberService;
import lombok.extern.slf4j.Slf4j;
import org.flowable.common.engine.api.delegate.event.FlowableEngineEntityEvent;
import org.flowable.common.engine.api.delegate.event.FlowableEngineEventType;
import org.flowable.engine.TaskService;
import org.flowable.engine.delegate.event.AbstractFlowableEngineEventListener;
import org.flowable.task.api.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;

import static com.ksptool.bio.biz.qf.commons.QfProcTools.trunc;
import static com.ksptool.entities.Entities.assign;

/**
 * 任务重分配监听器
 * <p>
 * 监听引擎 TASK_ASSIGNED 事件，当已创建的待办任务被重新分配办理人时触发。
 * 将原待办（QfTodoPo）状态置为已作废（status=10），同时根据新的办理人创建一条新待办。
 * <p>
 * 典型触发场景：整改流程中整改人变更、管理员手动变更任务办理人。
 *
 * @author 周彬(961523633@qq.com)
 * @since 2026-05-22
 * @license Proprietary
 * 版权所有 (c) 2026 KspTool及其贡献者保留所有权利。
 * 未经事先书面许可，严禁任何形式的复制或分发。
 */
@Slf4j
@Component
public class QfTaskAssignedListener extends AbstractFlowableEngineEventListener {
    @Lazy
    @Autowired
    private QfTodoRepository qfTodoRepository;
    @Lazy
    @Autowired
    private ApplicationEventPublisher aep;

    @Lazy
    @Autowired
    private QfMemberService qms;

    @Lazy
    @Autowired
    private TaskService taskService;

    public QfTaskAssignedListener() {
        super(Set.of(FlowableEngineEventType.TASK_ASSIGNED));
    }

    /**
     * 任务重分配事件回调
     * <p>
     * 当引擎任务发生重新分配时，将旧待办作废（status=10），
     * 并根据任务变量重新创建一条新待办及发布任务重分配事件。
     *
     * @param event 任务重分配事件
     */
    @Override
    protected void taskAssigned(FlowableEngineEntityEvent event) {
        Task task = (Task) event.getEntity();
        var po = qfTodoRepository.findByEngTaskId(task.getId());

        if (po == null) {
            log.warn("[QfTaskAssignedListener] 未找到对应待办记录, 引擎任务ID: {}", task.getId());
            return;
        }

        if (po.getStatus() != 0) {
            return;
        }
        // 取消旧待办
        po.setStatus(10);
        po.setComment("处理人变更，重新分配");
        qfTodoRepository.save(po);


        //获取任务变量Map
        Map<String, Object> vars = taskService.getVariables(task.getId());

        //获取办理人类型
        var memberKind = qms.getMemberKind(task);

        if (memberKind == null) {
            log.warn("待办任务创建失败: 无法解析办理人类型, 任务ID: {}", task.getId());
            return;
        }

        //获取办理成员ID
        var _memberId = qms.getMemberId(task);

        if (_memberId == null) {
            log.warn("待办任务创建失败: 无法解析办理成员ID, 任务ID: {}", task.getId());
            return;
        }

        //准备待办数据
        var rid = QfProcTools.varLong(vars, QfVarsProc.ROOT_ID, 0L);
        var did = QfProcTools.varLong(vars, QfVarsProc.DEPT_ID, 0L);
        var etId = task.getId();
        var epId = task.getProcessInstanceId();

        //获取具体的业务表单
        var bizFormId = QfProcTools.varLong(vars, QfVarsProc.BIZ_FORM_ID, 0L);
        var tableName = QfProcTools.varString(vars, QfVarsProc.TABLE_NAME, "unknow");
        var dataId = QfProcTools.varLong(vars, QfVarsProc.DATA_ID, 0L);
        var nodeName = QfProcTools.nodeName(task);
        var summary = QfProcTools.varString(vars, QfVarsProc.SUMMARY, "");
        var memberType = 0;

        if(memberKind == QfMemberKinds.USER){
            memberType = 0;
        }
        if(memberKind == QfMemberKinds.GROUP){
            memberType = 1;
        }

        var memberId = _memberId;
        var initiatorId = QfProcTools.varLong(vars, QfVarsProc.INITIATOR_ID, 0L);
        var initiatorName = QfProcTools.varString(vars, QfVarsProc.INITIATOR_NAME, "");
        var initiatorTime = QfProcTools.varDateTime(vars, QfVarsProc.INITIATOR_TIME, LocalDateTime.now());

        po = new QfTodoPo();
        po.setRootId(rid);
        po.setDeptId(did);
        po.setEngTaskId(etId);
        po.setEngProcId(epId);
        po.setBizFormId(bizFormId);
        po.setTableName(trunc(tableName, 200));
        po.setDataId(dataId);
        po.setNodeName(trunc(nodeName, 80));
        po.setSummary(trunc(summary, 500));
        po.setMemberType(memberType);
        po.setMemberId(memberId);
        po.setInitiatorId(initiatorId);
        po.setInitiatorName(trunc(initiatorName, 20));
        po.setInitiatorTime(initiatorTime);
        po.setStatus(0); //0:待办 1:已办 10:已作废

        qfTodoRepository.save(po);

        //发布任务启动事件
        QfTaskAssignedEvent fireEvent = new QfTaskAssignedEvent(this);
        assign(po, fireEvent);
        fireEvent.setTodoId(po.getId());
        aep.publishEvent(fireEvent);
    }

}
