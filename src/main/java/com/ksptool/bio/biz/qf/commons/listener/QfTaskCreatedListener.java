package com.ksptool.bio.biz.qf.commons.listener;

import com.ksptool.bio.biz.qf.commons.QfMemberKinds;
import com.ksptool.bio.biz.qf.commons.QfProcTools;
import com.ksptool.bio.biz.qf.commons.QfVarsProc;
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
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;
import static com.ksptool.bio.biz.qf.commons.QfProcTools.trunc;
/**
 * Flowable 任务创建监听器
 * 监听引擎 TASK_CREATED 事件，将每个新生成的UserTask映射为一条 QfTodoPo (待办)。
 * 业务相关数据(bizFormId/tableName/dataId/summary/initiator/rootId/deptId)
 * 由流程发起方 (launchQfProcess) 通过流程变量带入，监听器从变量中读取并兜底默认值。
 * 注意：监听器运行在引擎回调线程，没有Web会话上下文，所以 rootId/deptId 必须由变量提供，
 * 避免触发 QfTodoPo.onCreate 里的 SessionService.session() 抛 AuthException。
 */
@Slf4j
@Component
public class QfTaskCreatedListener extends AbstractFlowableEngineEventListener {

    @Autowired
    private QfTodoRepository qfTodoRepository;

    @Autowired
    private TaskService taskService;

    @Autowired
    private QfMemberService qms;

    public QfTaskCreatedListener() {
        // 仅订阅 TASK_CREATED, 其他事件无需回调
        super(Set.of(FlowableEngineEventType.TASK_CREATED));
    }

    /**
     * 任务创建事件回调
     * 这个方法会在任务创建时被调用
     * 在这个方法中，可以获取任务的变量，任务的办理成员，任务的节点名称，任务的摘要，任务的发起人，任务的发起时间，任务的所属企业/租户ID，任务的所属部门ID
     * 然后根据这些数据，创建一条待办数据
     *
     * @param event 任务创建事件，这个事件包含了任务的详细信息
     */
    @Override
    protected void taskCreated(FlowableEngineEntityEvent event) {

        //获取任务
        Task task = (Task) event.getEntity();

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


        //创建待办数据
        QfTodoPo po = new QfTodoPo();
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
        qfTodoRepository.save(po);
    }


}
