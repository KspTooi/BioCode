package com.ksptool.bio.biz.qf.commons.listener;

import com.ksptool.bio.biz.qf.commons.QfProcTools;
import com.ksptool.bio.biz.qf.commons.QfProcVars;
import com.ksptool.bio.biz.qf.model.qftodo.QfTodoPo;
import com.ksptool.bio.biz.qf.repository.QfTodoRepository;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.flowable.common.engine.api.delegate.event.FlowableEngineEntityEvent;
import org.flowable.common.engine.api.delegate.event.FlowableEngineEventType;
import org.flowable.engine.TaskService;
import org.flowable.engine.delegate.event.AbstractFlowableEngineEventListener;
import org.flowable.identitylink.api.IdentityLink;
import org.flowable.identitylink.api.IdentityLinkType;
import org.flowable.task.api.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Flowable 任务创建监听器
 * 监听引擎 TASK_CREATED 事件，将每个新生成的UserTask映射为一条 QfTodoPo (待办)。
 * 业务相关数据(bizFormId/tableName/dataId/summary/initiator/rootId/deptId)
 * 由流程发起方 (launchQfProcess) 通过流程变量带入，监听器从变量中读取并兜底默认值。
 * 注意：监听器运行在引擎回调线程，没有Web会话上下文，所以 rootId/deptId 必须由变量提供，
 * 避免触发 QfTodoPo.onCreate 里的 SessionService.session() 抛 AuthException。
 */
@Component
public class QfTaskCreatedListener extends AbstractFlowableEngineEventListener {

    @Autowired
    private QfTodoRepository qfTodoRepository;

    @Autowired
    private TaskService taskService;


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

        // 解析办理成员: 优先 assignee(具体办理人), 否则取首个 CANDIDATE 组
        int memberType = 0;
        long memberId = 0L;
        if (StringUtils.isNotBlank(task.getAssignee())) {
            memberType = 0;
            memberId = NumberUtils.toLong(task.getAssignee(), 0L);
        }
        if (StringUtils.isBlank(task.getAssignee())) {
            List<IdentityLink> links = taskService.getIdentityLinksForTask(task.getId());
            if (links != null) {
                for (IdentityLink link : links) {
                    if (!Objects.equals(IdentityLinkType.CANDIDATE, link.getType())) {
                        continue;
                    }
                    if (StringUtils.isBlank(link.getGroupId())) {
                        continue;
                    }
                    memberType = 1;
                    memberId = NumberUtils.toLong(link.getGroupId(), 0L);
                    break;
                }
            }
        }

        //准备待办数据


        QfTodoPo po = new QfTodoPo();
        po.setEngTaskId(task.getId());
        po.setEngProcId(task.getProcessInstanceId());
        po.setBizFormId(QfProcTools.varLong(vars, QfProcVars.BIZ_FORM_ID, 0L));
        po.setTableName(trunc(varString(vars, QfProcVars.TABLE_NAME, "test_table"), 200));
        po.setDataId(varLong(vars, QfProcVars.DATA_ID, 0L));
        po.setNodeName(trunc(nodeName(task), 80));
        po.setSummary(trunc(varString(vars, QfProcVars.SUMMARY, ""), 500));
        po.setMemberType(memberType);
        po.setMemberId(memberId);
        po.setInitiatorId(varLong(vars, QfProcVars.INITIATOR_ID, 0L));
        po.setInitiatorName(trunc(varString(vars, QfProcVars.INITIATOR_NAME, ""), 20));
        po.setInitiatorTime(varDateTime(vars, QfProcVars.INITIATOR_TIME, LocalDateTime.now()));
        // 提前 setRootId/setDeptId, 以短路 QfTodoPo.onCreate 对 SessionService 的强依赖
        po.setRootId(varLong(vars, QfProcVars.ROOT_ID, 0L));
        po.setDeptId(varLong(vars, QfProcVars.DEPT_ID, 0L));

        qfTodoRepository.save(po);
    }


}
