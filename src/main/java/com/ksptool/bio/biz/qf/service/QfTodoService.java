package com.ksptool.bio.biz.qf.service;

import com.ksptool.assembly.entity.exception.AuthException;
import com.ksptool.assembly.entity.exception.BizException;
import com.ksptool.assembly.entity.web.CommonIdDto;
import com.ksptool.assembly.entity.web.PageResult;
import com.ksptool.bio.biz.auth.repository.UserGroupRepository;
import com.ksptool.bio.biz.core.common.TupleMapper;
import com.ksptool.bio.biz.core.repository.UserRepository;
import com.ksptool.bio.biz.qf.commons.qfe.QfeBpmnModel;
import com.ksptool.bio.biz.qf.commons.qfe.QfeUserTask;
import com.ksptool.bio.biz.qf.commons.qfe.QfeUserTask.AprAction;
import com.ksptool.bio.biz.qf.commons.qfe.QfeUserTask.MemberKind;
import com.ksptool.bio.biz.qf.commons.qfe.QfeUserTask.TodoStatus;
import com.ksptool.bio.biz.qf.commons.QfeVarsModel;
import com.ksptool.bio.biz.qf.model.qfbizform.vo.GetQfBizFormDetailsVo;
import com.ksptool.bio.biz.qf.model.qfmodeldeployrcd.QfModelDeployRcdPo;
import com.ksptool.bio.biz.qf.model.qftodo.QfTodoPo;
import com.ksptool.bio.biz.qf.model.qftodo.dto.*;
import com.ksptool.bio.biz.qf.model.qftodo.vo.*;
import com.ksptool.bio.biz.qf.repository.QfModelDeployRcdRepository;
import com.ksptool.bio.biz.qf.repository.QfTodoRepository;
import jakarta.persistence.Tuple;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.flowable.engine.IdentityService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.task.api.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.ksptool.bio.biz.auth.service.SessionService.session;
import static com.ksptool.entities.Entities.as;
import static com.ksptool.entities.Entities.assign;


/**
 * 待办事项服务
 *
 * @author WangQingHua(603484930@qq.com)
 * @author Akkarin(1075613357@qq.com)
 * @author (Ish)Yuumi(1144150092@qq.com)
 * @author KspTool(ksptool@outlook.com)
 * @license Proprietary
 * 版权所有 (c) 2026 KspTool及其贡献者保留所有权利。
 * 未经事先书面许可，严禁任何形式的复制或分发。
 * @since 2026-04-17
 */
@Slf4j
@Service
public class QfTodoService {

    @Autowired
    private QfTodoRepository repository;

    @Autowired
    private TaskService ftService;

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private QfMemberService qfMemberService;

    @Autowired
    private IdentityService fiService;

    @Autowired
    private QfBizFormService bizFormService;



    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserGroupRepository userGroupRepository;

    @Autowired
    private QfModelDeployRcdRepository qfModelDeployRcdRepository;

    /**
     * 查询待办事项列表
     *
     * @param dto 查询条件
     * @return 查询结果
     */
    public PageResult<GetQfTodoListVo> getQfTodoList(GetQfTodoListDto dto) throws AuthException {
        QfTodoPo query = new QfTodoPo();
        assign(dto, query);

        var uid = session().getUserId();
        var gIds = qfMemberService.getMemberGroupIds(uid);

        //如果用户组ID列表为空，则设置为-1 防止Hibernate查询报错
        if (gIds == null || gIds.isEmpty()) {
            gIds = List.of(-1L);
        }

        Page<Tuple> page = repository.getQfTodoList(dto, uid, gIds, dto.pageRequest());
        if (page.isEmpty()) {
            return PageResult.successWithEmpty();
        }

        List<GetQfTodoListVo> vos = TupleMapper.tupleAs(page.getContent(), GetQfTodoListVo.class);
        return PageResult.success(vos, (int) page.getTotalElements());
    }

    /**
     * 新增待办事项
     *
     * @param dto 新增条件
     */
    @Transactional(rollbackFor = Exception.class)
    public void addQfTodo(AddQfTodoDto dto) {
        QfTodoPo insertPo = as(dto, QfTodoPo.class);
        repository.save(insertPo);
    }

    /**
     * 编辑待办事项
     *
     * @param dto 编辑条件
     * @throws BizException 业务异常
     */
    @Transactional(rollbackFor = Exception.class)
    public void editQfTodo(EditQfTodoDto dto) throws BizException {
        QfTodoPo updatePo = repository.findById(dto.getId())
                .orElseThrow(() -> new BizException("更新失败,数据不存在或无权限访问."));
        assign(dto, updatePo);
        repository.save(updatePo);
    }

    /**
     * 查询待办事项详情
     *
     * @param dto 查询条件
     * @return 查询结果
     * @throws BizException 业务异常
     */
    public GetQfTodoDetailsVo getQfTodoDetails(CommonIdDto dto) throws BizException {
        QfTodoPo po = repository.findById(dto.getId())
                .orElseThrow(() -> new BizException("查询详情失败,数据不存在或无权限访问."));
        CommonIdDto commonIdDto = new CommonIdDto();
        commonIdDto.setId(po.getBizFormId());
        GetQfBizFormDetailsVo getQfBizFormDetailsVo = bizFormService.getBizFormDetails(commonIdDto);
        GetQfTodoDetailsVo details = as(po, GetQfTodoDetailsVo.class);
        details.setRouteMobile(getQfBizFormDetailsVo.getRouteMobile());
        details.setRoutePc(getQfBizFormDetailsVo.getRoutePc());
        fillQfTodoFormConfig(po, details);

        return details;
    }

    /**
     * 取消/作废待办事项
     *
     * @param dto 取消条件
     * @throws BizException 业务异常
     */
    @Transactional(rollbackFor = Exception.class)
    public void cancelQfTodo(CancelQfTodoDto dto) throws BizException {
        QfTodoPo po = repository.findById(dto.getId())
                .orElseThrow(() -> new BizException("待办不存在或无权限访问."));
        if (po.getStatus() != TodoStatus.PENDING.getValue()) {
            throw new BizException("只能取消待办状态的记录");
        }
        String engProcId = po.getEngProcId();
        if (StringUtils.isNotBlank(engProcId)) {
            try {
                runtimeService.deleteProcessInstance(engProcId, dto.getReason());
            } catch (Exception e) {
                log.error("[cancelQfTodo] 终止Flowable流程实例失败, processInstanceId: {}", engProcId, e);
            }
        }
        po.setStatus(TodoStatus.CANCELLED.getValue());
        po.setComment(dto.getReason());
        repository.save(po);
    }

    /**
     * 根据表名和数据ID批量取消待办事项
     *
     * @param tableName 表名
     * @param dataId   数据ID
     * @param reason   取消原因
     */
    @Transactional(rollbackFor = Exception.class)
    public void cancelQfTodoByData(String tableName, Long dataId, String reason) {
        List<QfTodoPo> todoList = repository.getByTableNameAndDataId(tableName, dataId);
        Set<String> deletedProcIds = new HashSet<>();
        for (QfTodoPo po : todoList) {
            if (po.getStatus() != TodoStatus.PENDING.getValue()) {
                continue;
            }
            String engProcId = po.getEngProcId();
            if (StringUtils.isNotBlank(engProcId) && !deletedProcIds.contains(engProcId)) {
                try {
                    runtimeService.deleteProcessInstance(engProcId, reason);
                    deletedProcIds.add(engProcId);
                } catch (Exception e) {
                    log.error("[cancelQfTodoByData] 终止Flowable流程实例失败, processInstanceId: {}", engProcId, e);
                }
            }
            po.setStatus(TodoStatus.CANCELLED.getValue());
            po.setComment(reason);
            repository.save(po);
        }
    }

    /**
     * 删除待办事项
     *
     * @param dto 删除条件
     * @throws BizException 业务异常
     */
    @Transactional(rollbackFor = Exception.class)
    public void removeQfTodo(CommonIdDto dto) throws BizException {
        List<Long> ids = dto.isBatch() ? dto.getIds() : List.of(dto.getId());
        Set<String> deletedProcIds = new HashSet<>();
        for (Long id : ids) {
            QfTodoPo po = repository.findById(id).orElse(null);
            if (po == null) {
                continue;
            }
            if (po.getStatus() != TodoStatus.PENDING.getValue()) {
                throw new BizException("只能删除待办状态的记录");
            }

            //todo 这里估计要和帅龙讨论一下 删除代办要不要终止流程
            String engProcId = po.getEngProcId();
            if (StringUtils.isNotBlank(engProcId) && !deletedProcIds.contains(engProcId)) {
                try {
                    runtimeService.deleteProcessInstance(engProcId, "待办已删除");
                    deletedProcIds.add(engProcId);
                } catch (Exception e) {
                    log.error("[removeQfTodo] 终止Flowable流程实例失败, processInstanceId: {}", engProcId, e);
                }
            }
        }
        if (dto.isBatch()) {
            repository.deleteAllById(dto.getIds());
            return;
        }
        repository.deleteById(dto.getId());
    }

    /**
     * 审批待办事项
     *
     * @param dto 审批条件
     * @throws BizException 业务异常
     */
    @Transactional(rollbackFor = Exception.class)
    public void approveQfTodo(ApproveQfTodoDto dto) throws Exception {

        QfTodoPo updatePo = repository.findById(dto.getId())
                .orElseThrow(() -> new BizException("审批失败,数据不存在或无权限访问."));

        if (updatePo.getStatus() != TodoStatus.PENDING.getValue()) {
            throw new BizException("待办状态异常，无法审批.");
        }

        var aud = session();
        var uid = aud.getUserId();
        var userPo = userRepository.findById(uid)
                .orElseThrow(() -> new BizException("审批失败,当前用户不存在."));

        //判断是不是我的待办
        if (updatePo.getMemberType() == MemberKind.USER.getMemberType()) {

            //如果待办是给用户的，判断我是不是这个用户
            if (!Objects.equals(updatePo.getMemberId(), uid)) {
                throw new BizException("该待办属于用户:" + updatePo.getMemberId() + "，审批人不是本人，无法审批.");
            }

        }

        //如果待办是给用户组的，判断我是不是这个用户组的一员
        if (updatePo.getMemberType() == MemberKind.GROUP.getMemberType()) {
            var groupIds = qfMemberService.getMemberGroupIds(updatePo.getMemberId());
            if (!groupIds.contains(uid)) {
                throw new BizException("该待办属于用户组:" + updatePo.getMemberId() + "，审批人不是该用户组的一员，无法审批.");
            }

        }

        //先获取任务
        var task = ftService.createTaskQuery()
                .taskId(updatePo.getEngTaskId())
                .singleResult();

        if (task == null) {
            throw new BizException("引擎故障，无法获取任务信息.");
        }

        var comment = "";

        if (StringUtils.isNotBlank(dto.getComment())) {
            comment = dto.getComment();
        }

        if (dto.getAction() == AprAction.TRANSFER.getValue()) {
            // 转交：由 QfTaskAssignedListener 自动作废旧待办 + 创建新待办
            var targetUid = dto.getMemberId();
            if (targetUid == null) {
                throw new BizException("转交时办理成员ID不能为空");
            }

            ftService.addComment(task.getId(), task.getProcessInstanceId(), comment);
            ftService.setAssignee(task.getId(), String.valueOf(targetUid));
            return;
        }

        if (dto.getAction() == AprAction.REJECT_TO_NODE.getValue()) {
            // 驳回到节点：使用 changeActivityState 迁移到指定节点
            if (StringUtils.isBlank(dto.getNodeId())) {
                throw new BizException("驳回到节点时节点ID不能为空");
            }

            fiService.setAuthenticatedUserId(String.valueOf(uid));
            try {
                runtimeService.createChangeActivityStateBuilder()
                        .processInstanceId(updatePo.getEngProcId())
                        .moveActivityIdTo(task.getTaskDefinitionKey(), dto.getNodeId())
                        .changeState();

                updatePo.setStatus(TodoStatus.DONE.getValue());
                updatePo.setFinMemberId(uid);
                updatePo.setFinMemberName(userPo.getNickname());
                updatePo.setFinTime(LocalDateTime.now());
                updatePo.setAction(dto.getAction());
                updatePo.setComment(comment);
                repository.save(updatePo);

                cancelOrphanedTodos(updatePo.getEngProcId(), updatePo.getId());
            } finally {
                fiService.setAuthenticatedUserId(null);
            }
            return;
        }

        // action=0(同意) / action=1(驳回)：走引擎 complete 流程
        var vars = new HashMap<String, Object>();
        vars.put("approved", dto.getAction() == AprAction.AGREE.getValue());   // 用于排他网关走向 true/false 分支
        vars.put("comment", comment);     // 审批意见

        //设置审批人(为了让 Flowable 在历史表 ACT_HI_TASKINST 记录"谁办的)
        fiService.setAuthenticatedUserId(String.valueOf(uid));

        try {

            //这为了走 Flowable 自己的评论体系（历史记录会看到），在 complete 之前加：
            ftService.addComment(task.getId(), task.getProcessInstanceId(), comment);

            //审批任务
            ftService.complete(task.getId(), vars);

            //或签多实例：完成任务后，检查同级待办是否已被引擎取消，作废之
            cancelOrphanedTodos(updatePo.getEngProcId(), updatePo.getId());

            //更新待办状态为已办
            updatePo.setStatus(TodoStatus.DONE.getValue());

            //更新实际办理人
            updatePo.setFinMemberId(uid);
            //todo 获取用户昵称 目前临时从core域获取，以后还是会从auth域session中获取
            updatePo.setFinMemberName(userPo.getNickname());
            updatePo.setFinTime(LocalDateTime.now());
            updatePo.setAction(dto.getAction());
            updatePo.setComment(comment);
            repository.save(updatePo);

        } finally {
            fiService.setAuthenticatedUserId(null);
        }

    }





    /**
     * 作废同流程中已被引擎取消的孤儿待办
     * <p>
     * 或签多实例场景：一个任务完成后，引擎取消其余并行实例，
     * 但无 TASK_CANCELLED 事件可捕获。本方法通过比对 Flowable 活跃任务，
     * 找出那些引擎任务已不存在但待办仍为 status=0 的记录并作废。
     */
    private void cancelOrphanedTodos(String engProcId, Long completedTodoId) {
        List<QfTodoPo> pendingTodos = repository.findAllByEngProcIdAndStatus(engProcId, TodoStatus.PENDING.getValue());
        if (pendingTodos.isEmpty()) {
            return;
        }
        //收集所有活跃的引擎任务ID
        List<Task> activeTasks = ftService.createTaskQuery()
                .processInstanceId(engProcId)
                .list();
        Set<String> activeTaskIds = activeTasks.stream()
                .map(Task::getId)
                .collect(Collectors.toSet());
        for (QfTodoPo todo : pendingTodos) {
            if (todo.getId().equals(completedTodoId)) {
                continue;
            }
            if (activeTaskIds.contains(todo.getEngTaskId())) {

                continue;
            }
            //引擎任务已被取消，作废待办
            todo.setStatus(TodoStatus.CANCELLED.getValue());
            todo.setComment("或签触发，其余待办自动作废");
            repository.save(todo);
            log.info("[cancelOrphanedTodos] 待办已作废, todoId: {}, engTaskId: {}", todo.getId(), todo.getEngTaskId());
        }
    }

    /**
     * 按业务数据ID批量汇总各数据当前待办的办理用户ID
     * <p>
     * 用户/任意人类型直接取 memberId;用户组类型批量展开为组内成员用户ID。
     *
     * @param dataIds 业务数据ID列表
     * @return dataId -> 办理用户ID列表
     */
    public Map<Long, List<Long>> getQfTodoListByIds(List<Long> dataIds) {
        Map<Long, List<Long>> dataIdToUserIds = new HashMap<>();
        List<QfTodoPo> qfTodoPos = repository.getAllByDataIdAndStatus(dataIds, TodoStatus.PENDING.getValue());
        List<QfTodoPo> groupTodos = qfTodoPos.stream().filter(v -> v.getMemberType() == MemberKind.GROUP.getMemberType()).toList();
        List<QfTodoPo> userTodos = qfTodoPos.stream().filter(v -> v.getMemberType() == MemberKind.USER.getMemberType()).toList();
        List<QfTodoPo> anyoneTodos = qfTodoPos.stream().filter(v -> v.getMemberType() == MemberKind.ANYONE.getMemberType()).toList();

        //用户类型：直接按 dataId 归集 memberId
        for (QfTodoPo todo : userTodos) {
            dataIdToUserIds.computeIfAbsent(todo.getDataId(), k -> new ArrayList<>()).add(todo.getMemberId());
        }
        //任意人类型：直接按 dataId 归集 memberId
        for (QfTodoPo todo : anyoneTodos) {
            dataIdToUserIds.computeIfAbsent(todo.getDataId(), k -> new ArrayList<>()).add(todo.getMemberId());
        }
        //用户组类型：批量查询组成员，内存按 groupId 分组后展开
        if (!groupTodos.isEmpty()) {
            List<Long> groupIds = groupTodos.stream().map(QfTodoPo::getMemberId).distinct().toList();
            Map<Long, List<Long>> groupUserMap = new HashMap<>();
            userGroupRepository.findAllByGroupIds(groupIds).forEach(ug ->
                    groupUserMap.computeIfAbsent(ug.getGroupId(), k -> new ArrayList<>()).add(ug.getUserId())
            );
            for (QfTodoPo todo : groupTodos) {
                List<Long> userIds = groupUserMap.get(todo.getMemberId());
                if (userIds != null && !userIds.isEmpty()) {
                    dataIdToUserIds.computeIfAbsent(todo.getDataId(), k -> new ArrayList<>()).addAll(userIds);
                }
            }
        }
        return dataIdToUserIds;
    }
    /**
     * 填充待办事项的表单配置（审批操作按钮、是否允许填写意见、可编辑字段）
     * <p>
     * 从 qf_model_deploy_rcd 部署表的 BPMN XML 中按节点名称定位 UserTask，读取其 QFE 扩展属性。
     *
     * @param po 待办记录
     * @param vo 待填充的详情 Vo
     */
    private void fillQfTodoFormConfig(QfTodoPo po, GetQfTodoDetailsVo vo) {
        vo.setAllowComment(1);

        List<GetQfTodoDetailsVo.OperationConfig> operations = new ArrayList<>();

        QfeUserTask ut = findDeployUserTaskByName(po.getEngProcessDefId(), po.getNodeName());
        if (ut != null) {
            // utAprComment：1=允许填写审批意见, 0=不允许；缺省按允许处理
            vo.setAllowComment(NumberUtils.toInt(ut.getAttr(QfeVarsModel.UT_APR_COMMENT), 1));

            List<String> editFields = ut.getFormAllowEditFields();
            if (!editFields.isEmpty()) {
                vo.setAllowEditFields(editFields);
            }

            // utAprActions(操作值) 与 utAprActionNames(显示名) 按下标一一对应
            List<Integer> actions = ut.getActions();
            List<String> actionNames = ut.getActionNames();
            int len = Math.min(actions.size(), actionNames.size());
            for (int i = 0; i < len; i++) {
                var op = new GetQfTodoDetailsVo.OperationConfig();
                op.setKind(actions.get(i));
                op.setName(actionNames.get(i));
                operations.add(op);
            }
        }

        // 若未从扩展属性中读到有效按钮，使用默认值（同意/驳回）
        if (operations.isEmpty()) {
            var agree = new GetQfTodoDetailsVo.OperationConfig();
            agree.setKind(AprAction.AGREE.getValue());
            agree.setName("同意");
            operations.add(agree);

            var reject = new GetQfTodoDetailsVo.OperationConfig();
            reject.setKind(AprAction.REJECT.getValue());
            reject.setName("驳回");
            operations.add(reject);
        }

        vo.setAllowActions(operations);
    }

    /**
     * 从部署表 BPMN XML 中按节点名称定位 QFE UserTask 包装
     *
     * @param processDefinitionId 引擎流程定义ID
     * @param nodeName            节点名称
     * @return QfeUserTask 包装，未命中返回 null
     */
    private QfeUserTask findDeployUserTaskByName(String processDefinitionId, String nodeName) {
        QfeBpmnModel model = loadDeployModel(processDefinitionId);
        if (model == null) {
            return null;
        }
        return model.getUserTaskByName(nodeName);
    }

    /**
     * 从 qf_model_deploy_rcd 部署表读取 BPMN XML 并包装为 QfeBpmnModel
     *
     * @param processDefinitionId 引擎流程定义ID
     * @return QfeBpmnModel，无部署记录或 XML 为空时返回 null
     */
    private QfeBpmnModel loadDeployModel(String processDefinitionId) {
        QfModelDeployRcdPo deployRcd = qfModelDeployRcdRepository.findByEngProcessDefId(processDefinitionId);
        if (deployRcd == null || StringUtils.isBlank(deployRcd.getBpmnXml())) {
            return null;
        }
        return new QfeBpmnModel().of(deployRcd.getBpmnXml());
    }

    /**
     * 获取当前节点之前的历史节点列表（供驳回选择）
     * <p>
     * 由 QfeBpmnModel 反向追溯上游 UserTask，再与已办记录按节点名取交集。
     *
     * @param dto 当前待办ID
     * @return 上游已办节点列表
     * @throws BizException 业务异常
     */
    public List<ProcessNodeVo> getQfTodoProcessNodes(CommonIdDto dto) throws BizException {
        QfTodoPo po = repository.findById(dto.getId())
                .orElseThrow(() -> new BizException("查询失败,数据不存在或无权限访问."));

        QfeBpmnModel model = loadDeployModel(po.getEngProcessDefId());
        if (model == null) {
            return List.of();
        }

        // 1. 反向追溯上游 UserTask，建立 节点名 -> 节点ID 映射
        Map<String, String> nodeNameToId = new HashMap<>();
        for (var ut : model.getUpstreamUserTasks(po.getNodeName())) {
            nodeNameToId.putIfAbsent(ut.getName(), ut.getId());
        }
        if (nodeNameToId.isEmpty()) {
            return List.of();
        }

        // 2. 查询已办记录，只返回属于上游节点集合的
        List<QfTodoPo> historyTodos = repository.findAllByEngProcIdAndStatus(po.getEngProcId(), TodoStatus.DONE.getValue());
        if (historyTodos.isEmpty()) {
            return List.of();
        }

        Set<String> seen = new HashSet<>();
        List<ProcessNodeVo> result = new ArrayList<>();
        for (var todo : historyTodos) {
            if (!seen.add(todo.getNodeName())) {
                continue;
            }
            String nodeId = nodeNameToId.get(todo.getNodeName());
            if (nodeId == null) {
                continue;
            }
            ProcessNodeVo vo = new ProcessNodeVo();
            vo.setNodeId(nodeId);
            vo.setNodeName(todo.getNodeName());
            result.add(vo);
        }
        return result;
    }

}

