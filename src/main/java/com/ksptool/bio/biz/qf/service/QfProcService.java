package com.ksptool.bio.biz.qf.service;

import com.ksptool.assembly.entity.exception.AuthException;
import com.ksptool.assembly.entity.exception.BizException;
import com.ksptool.bio.biz.auth.service.SessionService;
import com.ksptool.bio.biz.core.model.user.UserPo;
import com.ksptool.bio.biz.core.repository.UserRepository;
import com.ksptool.bio.biz.qf.commons.QfeVarsModel;
import com.ksptool.bio.biz.qf.commons.QfVarsProc;
import com.ksptool.bio.biz.qf.commons.qfe.QfeBpmnModel;
import com.ksptool.bio.biz.qf.commons.qfe.QfeUserTask;
import com.ksptool.bio.biz.qf.commons.qfe.QfeUserTask.MemberKind;
import com.ksptool.bio.biz.qf.commons.util.Flowable8NodeUtil;
import com.ksptool.bio.biz.qf.model.qftodo.QfTodoPo;
import com.ksptool.bio.biz.qf.model.qftodo.dto.GetProcessApproveFlowDto;
import com.ksptool.bio.biz.qf.model.qftodo.dto.GetProcessApproveFlowRecordDto;
import com.ksptool.bio.biz.qf.model.qftodo.vo.ApproveFlowRecordVo;
import com.ksptool.bio.biz.qf.model.qftodo.vo.ProcessNodeConfigVo;
import com.ksptool.bio.biz.qf.repository.QfBizFormRepository;
import com.ksptool.bio.biz.qf.repository.QfModelDeployRcdRepository;
import com.ksptool.bio.biz.qf.repository.QfTodoRepository;
import com.ksptool.text.PreparedPrompt;
import jakarta.validation.Valid;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.flowable.engine.IdentityService;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.runtime.ProcessInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 流程管理器
 * 这个服务用于管理流程的启动，暂停，恢复，终止等操作
 *
 * @author Akkarin(1075613357@qq.com)
 * @author (Ish)Yuumi(1144150092@qq.com)
 * @author KspTool(ksptool@outlook.com)
 * @license Proprietary
 * 版权所有 (c) 2026 KspTool及其贡献者保留所有权利。
 * 未经事先书面许可，严禁任何形式的复制或分发。
 * @since 2026-04-17
 */
@Service
public class QfProcService {

    //时间格式化器(yyyy-MM-dd HH:mm:ss)
    private static final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Autowired
    private QfModelDeployRcdRepository qmdrRepository;

    @Autowired
    private QfBizFormRepository qbfRepository;

    @Autowired
    private RepositoryService frpService;

    @Autowired
    private RuntimeService frService;

    @Autowired
    private IdentityService fiService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private Flowable8NodeUtil flowable8NodeUtil;

    @Autowired
    private QfTodoRepository qfTodoRepository;

    /**
     * 按模型编码发起审批流程(测试)
     *
     * @param modelCode   模型编码
     * @param dataId      业务数据ID
     * @return 流程实例ID
     * @throws BizException 业务异常
     */
    @Transactional(rollbackFor = Exception.class)
    public String launchProc(String modelCode,  Long dataId, Map<String,String> datas) throws BizException, AuthException {

        if (StringUtils.isBlank(modelCode)) {
            throw new BizException("无法启动流程,模型编码不能为空");
        }

        if (dataId == null) {
            throw new BizException("无法启动流程,业务数据ID不能为空");
        }

        var deploy = qmdrRepository.getLatestActiveByCode(modelCode);


        if (deploy == null) {
            throw new BizException("无法启动流程,未找到可用的流程部署[code=" + modelCode + "]");
        }

        var form = qbfRepository.findById(deploy.getFormId()).get();

        var aud = SessionService.session();
        var rootId = aud.getRootId();
        var deptId = aud.getDeptId();
        var userId = aud.getUserId();
        var nickname = aud.getNickname();

        if (rootId == null || deptId == null) {
            throw new BizException("无法启动流程,未能获取到有效的租户ID或者部门ID！");
        }

        if (userId == null) {
            throw new BizException("无法启动流程,未能获取到有效的用户ID！");
        }

        if (StringUtils.isBlank(nickname)) {
            nickname = aud.getUsername();
        }

        var p = new HashMap<String, Object>();

        p.put("initiator", userId.toString());
        p.put(QfVarsProc.ROOT_ID.getValue(), rootId);
        p.put(QfVarsProc.DEPT_ID.getValue(), deptId);
        p.put(QfVarsProc.INITIATOR_ID.getValue(), userId);
        p.put(QfVarsProc.INITIATOR_NAME.getValue(), nickname);
        p.put(QfVarsProc.INITIATOR_TIME.getValue(), LocalDateTime.now().format(dtf));

        p.put(QfVarsProc.BIZ_FORM_ID.getValue(), form.getId());
        p.put(QfVarsProc.TABLE_NAME.getValue(), form.getTableName());
        p.put(QfVarsProc.DATA_ID.getValue(), dataId);

        String summary = nickname + "提交的" + form.getName() + "审批";
        if (StringUtils.isNotBlank(form.getSummaryTemplate())) {
            PreparedPrompt prompt = new PreparedPrompt(form.getSummaryTemplate());
            for (var entry : datas.entrySet()) {
                prompt.setParameter(entry.getKey(), entry.getValue());
            }
            summary = prompt.execute();
        }
        p.put(QfVarsProc.SUMMARY.getValue(), summary);

        fiService.setAuthenticatedUserId(userId.toString());

        var qfeModel = new QfeBpmnModel().of(frpService.getBpmnModel(deploy.getEngProcessDefId()));
        if (qfeModel.getBpmnModel() == null || qfeModel.getBpmnModel().getMainProcess() == null) {
            throw new BizException("无法启动流程,未找到可用的流程模型[code=" + deploy.getCode() + "]");
        }

        prepareUserTaskVars(qfeModel.getUserTasks(), p, datas);

        try {
            ProcessInstance pi = frService.startProcessInstanceById(
                    deploy.getEngProcessDefId(),
                    dataId.toString(),
                    p);

            return pi.getId();

        } finally {
            fiService.setAuthenticatedUserId(null);
        }

    }


    /**
     * 一次性遍历所有 UserTask，准备流程变量
     * <p>
     * 发起时选人节点（utAprKind=1）从业务数据注入发起人所选办理人；其余多实例节点从模型配置注入候选人。
     */
    private void prepareUserTaskVars(List<QfeUserTask> userTasks, Map<String, Object> p, Map<String, String> datas) throws BizException {
        if (userTasks.isEmpty()) {
            return;
        }

        for (QfeUserTask ut : userTasks) {
            boolean isMulti = ut.isMultiInstance();

            // 发起时选人：从业务数据注入发起人所选办理人
            if (ut.isInitSelected()) {
                var memberKind = ut.getMemberKind();
                if (memberKind == null) {
                    throw new BizException("节点[" + ut.getId() + "]配置为发起时选人,但无法解析审批人类型");
                }
                if (isMulti) {
                    injectMultiInstanceApprover(ut, "qfMi_" + ut.getId(), memberKind, p, datas);
                } else {
                    injectSingleApprover(ut, memberKind, p, datas);
                }
            }

            // 多实例且未通过发起时选人设置：从模型配置注入候选人
            if (isMulti && !p.containsKey("qfMi_" + ut.getId())) {
                var values = ut.getMemberIds();
                if (values.isEmpty()) {
                    throw new BizException("多实例节点[" + ut.getId() + "]未配置候选人");
                }
                p.put("qfMi_" + ut.getId(), values.stream().map(String::valueOf).toList());
            }
        }
    }

    /**
     * 单实例发起时选人：取发起人所选，按节点范围（utAprMemberIds）校验后注入节点级流程变量
     * <p>
     * 选人结果以节点独立流程变量带入(qfAprNode_&lt;节点ID&gt; / qfAprGroup_&lt;节点ID&gt;)，
     * 任务创建时由 QfMemberService.getMemberId 据此解析办理人，故下游节点同样生效；
     * 节点范围仅用于校验发起人所选是否越界，不作为候选直接注入；
     * 仅支持 任意人 / 指定用户 / 用户组（与 QfeBpmnModel.validateUserTasks 的约束一致）。
     */
    private void injectSingleApprover(QfeUserTask ut, MemberKind memberKind,
                                      Map<String, Object> p, Map<String, String> datas) throws BizException {
        // 任意人：发起人任选一人，无范围校验
        if (memberKind == MemberKind.ANYONE) {
            String id = firstId(extractApproverId(datas));
            if (StringUtils.isBlank(id)) {
                throw new BizException("节点[" + ut.getId() + "]为任意人审批,业务表单数据中必须包含审批人ID(approverId)");
            }
            p.put("qfAprNode_" + ut.getId(), id);
            return;
        }

        // 指定用户：发起人从范围内选人，校验所选用户在范围内
        if (memberKind == MemberKind.USER) {
            String id = firstId(extractApproverId(datas));
            if (StringUtils.isBlank(id)) {
                throw new BizException("节点[" + ut.getId() + "]为发起时指定用户,业务表单数据中必须包含审批人ID(approverId)");
            }
            validateInScope(ut, id);
            p.put("qfAprNode_" + ut.getId(), id);
            return;
        }

        // 用户组：发起人从范围内选组，校验所选组在范围内
        if (memberKind == MemberKind.GROUP) {
            String groupId = firstId(extractGroupIds(datas));
            if (StringUtils.isBlank(groupId)) {
                throw new BizException("节点[" + ut.getId() + "]为发起时指定用户组,业务表单数据中必须包含用户组ID(groupIds)");
            }
            validateInScope(ut, groupId);
            p.put("qfAprGroup_" + ut.getId(), groupId);
        }
    }

    /**
     * 多实例任务注入：把发起人所选的用户/用户组ID集合注入 collection 变量（qfMi_<节点ID>）
     */
    private void injectMultiInstanceApprover(QfeUserTask ut, String varName, MemberKind memberKind,
                                             Map<String, Object> p, Map<String, String> datas) throws BizException {

        // 任意人 / 指定用户：注入用户ID集合，指定用户需逐个校验在范围内
        if (memberKind == MemberKind.ANYONE || memberKind == MemberKind.USER) {
            List<String> approverIds = parseApproverIds(extractApproverId(datas));
            if (approverIds.isEmpty()) {
                throw new BizException("节点[" + ut.getId() + "]业务表单数据中必须包含审批人ID列表");
            }
            if (memberKind == MemberKind.USER) {
                for (String id : approverIds) {
                    validateInScope(ut, id);
                }
            }
            p.put(varName, approverIds);
            return;
        }

        // 用户组：注入用户组ID集合，逐个校验在范围内
        if (memberKind == MemberKind.GROUP) {
            List<String> groupIds = parseApproverIds(extractGroupIds(datas));
            if (groupIds.isEmpty()) {
                throw new BizException("节点[" + ut.getId() + "]为发起时指定用户组,业务表单数据中必须包含用户组ID(groupIds)");
            }
            for (String id : groupIds) {
                validateInScope(ut, id);
            }
            p.put(varName, groupIds);
        }
    }

    /**
     * 校验发起人所选ID是否在节点配置的范围（utAprMemberIds）内
     *
     * @param ut         发起时选人节点
     * @param selectedId 发起人所选的用户ID或用户组ID
     * @throws BizException ID 格式错误或不在允许范围内时抛出
     */
    private void validateInScope(QfeUserTask ut, String selectedId) throws BizException {
        long id = NumberUtils.toLong(selectedId, 0L);
        if (id == 0L) {
            throw new BizException("ID格式错误: " + selectedId);
        }
        if (!ut.isInMemberScope(id)) {
            throw new BizException("选择的[" + selectedId + "]不在节点[" + ut.getId() + "]允许的范围内");
        }
    }

    /**
     * 取逗号分隔串的第一个元素并 trim；空串返回 null
     */
    private static String firstId(String csv) {
        if (StringUtils.isBlank(csv)) {
            return null;
        }
        return csv.split(",")[0].trim();
    }

    /**
     * 从业务数据中提取单个审批人ID
     */
    private String extractApproverId(Map<String, String> datas) {
        if (datas == null) {
            return null;
        }

        return datas.get("approverId");
    }

    /**
     * 从业务数据中提取组/部门ID列表(逗号分隔)
     */
    private String extractGroupIds(Map<String, String> datas) {
        if (datas == null) {
            return null;
        }
        String ids = datas.get("groupIds");
        if (StringUtils.isBlank(ids)) {
            ids = datas.get("deptIds");
        }
        if (StringUtils.isBlank(ids)) {
            ids = datas.get("candidateGroupIds");
        }
        return ids;
    }

    /**
     * 解析审批人ID列表(逗号分隔)
     */
    private List<String> parseApproverIds(String idsStr) throws BizException {
        if (StringUtils.isBlank(idsStr)) {
            return List.of();
        }
        String[] parts = idsStr.split(",");
        List<String> result = new ArrayList<>();
        for (String part : parts) {
            String trimmed = part.trim();
            if (StringUtils.isBlank(trimmed)) {
                continue;
            }
            long id;
            try {
                id = Long.parseLong(trimmed);
            } catch (NumberFormatException e) {
                throw new BizException("审批人ID格式错误: " + trimmed);
            }
            if (id == 0L) {
                continue;
            }
            result.add(trimmed);
        }
        return result;
    }

    /**
     * 获取流程所有节点的审批配置（发起流程时使用）
     * <p>
     * 遍历 BPMN 模型中的所有 UserTask 节点，返回每个节点的基本信息及办理成员配置。
     * 前端据此判断哪些节点需要发起人手动选择审批人（memberKind=10 任意人）。
     *
     * @param modelCode 模型编码
     * @return 节点配置列表
     * @throws BizException 业务异常
     */
    public List<ProcessNodeConfigVo> getProcessNodeConfigs(String modelCode) throws BizException {

        if (StringUtils.isBlank(modelCode)) {
            throw new BizException("模型编码不能为空");
        }

        var deploy = qmdrRepository.getLatestActiveByCode(modelCode);
        if (deploy == null) {
            throw new BizException("未找到可用的流程部署[code=" + modelCode + "]");
        }

        var qfeModel = new QfeBpmnModel().of(frpService.getBpmnModel(deploy.getEngProcessDefId()));
        if (qfeModel.getBpmnModel() == null || qfeModel.getBpmnModel().getMainProcess() == null) {
            return List.of();
        }

        List<ProcessNodeConfigVo> result = new ArrayList<>();
        // 收集需要查询名称的用户ID（memberKind=USER 且 BPMN 无 memberNames 时）
        Map<Integer, List<Long>> nodeIdsToLookup = new HashMap<>();

        for (QfeUserTask ut : qfeModel.getUserTasks()) {
            var vo = new ProcessNodeConfigVo();
            vo.setNodeId(ut.getId());
            vo.setNodeName(ut.getName());

            var memberKind = ut.getMemberKind();
            vo.setMemberKind(memberKind != null ? memberKind.getValue() : null);

            var approveKind = ut.getApproveKind();
            vo.setAprKind(approveKind != null ? approveKind.getValue() : 0);

            var memberIds = ut.getMemberIds();
            vo.setMemberIds(memberIds.stream().map(String::valueOf).toList());

            var memberNamesStr = ut.getAttr(QfeVarsModel.UT_APR_MEMBER_NAMES);
            if (StringUtils.isNotBlank(memberNamesStr)) {
                List<String> names = new ArrayList<>();
                for (String n : StringUtils.split(memberNamesStr, ",")) {
                    String t = StringUtils.trim(n);
                    if (StringUtils.isNotBlank(t)) {
                        names.add(t);
                    }
                }
                vo.setMemberNames(names);
            } else if (memberKind == MemberKind.USER && !memberIds.isEmpty()) {
                // BPMN 无名称且为指定用户类型，标记为待 lookup
                nodeIdsToLookup.put(result.size(), memberIds);
                vo.setMemberNames(List.of());
            } else {
                vo.setMemberNames(List.of());
            }

            result.add(vo);
        }

        // 批量查询用户名称
        if (!nodeIdsToLookup.isEmpty()) {
            var allUserIds = nodeIdsToLookup.values().stream()
                    .flatMap(List::stream)
                    .distinct()
                    .toList();
            Map<Long, String> idToName = userRepository.findAllById(allUserIds).stream()
                    .collect(Collectors.toMap(UserPo::getId, UserPo::getNickname, (a, b) -> a));

            for (var entry : nodeIdsToLookup.entrySet()) {
                var vo = result.get(entry.getKey());
                var names = entry.getValue().stream()
                        .map(id -> idToName.getOrDefault(id, String.valueOf(id)))
                        .toList();
                vo.setMemberNames(names);
            }
        }

        return result;
    }

    /**
     * 获取流程审批流转的着色 BPMN XML,用于前端按当前进度高亮节点
     *
     * @param dto 含流程实例ID
     * @return 着色后的 BPMN XML 字符串
     */
    public String getProcessApproveFlow(@Valid GetProcessApproveFlowDto dto) {
        return flowable8NodeUtil.generateColorBpmnXml(dto.getEngProcId());
    }

    /**
     * 获取待办事项流程流转记录
     * 按时间顺序返回：节点名称、节点审批人、节点审批时间、节点审批结果
     *
     * @param dto 查询条件
     * @return 流转记录列表
     * @throws BizException 业务异常
     */
    public List<ApproveFlowRecordVo> getProcessApproveFlowRecord(GetProcessApproveFlowRecordDto dto) throws BizException {

        // 通过流程ID查询该流程所有待办，即为流转记录
        List<QfTodoPo> todoList = qfTodoRepository.findAllByEngProcIdOrderByCreateTimeAsc(dto.getEngProcId());

        //获取所有人的用户信息
        //获取代办的信息
        List<QfTodoPo> waitingList = todoList.stream().filter(todo -> (todo.getStatus() == 0 || todo.getStatus() == 10)).toList();
        Map<Long, UserPo> userMap;
        if(!waitingList.isEmpty()) {
            List<UserPo> userList = userRepository.findAllById(waitingList.stream().map(QfTodoPo::getMemberId).toList());
            userMap = userList.stream().collect(Collectors.toMap(UserPo::getId, user -> user));
        } else {
            userMap = new HashMap<>();
        }
        return todoList.stream().map(todo -> {
            ApproveFlowRecordVo vo = new ApproveFlowRecordVo();
            vo.setNodeName(todo.getNodeName());
            if (StringUtils.isNotBlank(todo.getFinMemberName())) {
                vo.setFinMemberName(todo.getFinMemberName());
            } else {
                var userPo = userMap.get(todo.getMemberId());
                vo.setFinMemberName(userPo != null ? userPo.getNickname() : null);
            }
            vo.setFinTime(todo.getFinTime());
            vo.setAction(todo.getAction());
            vo.setComment(todo.getComment());
            vo.setStatus(todo.getStatus());
            return vo;
        }).toList();
    }
}
