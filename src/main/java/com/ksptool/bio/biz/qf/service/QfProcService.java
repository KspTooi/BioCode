package com.ksptool.bio.biz.qf.service;

import com.ksptool.assembly.entity.exception.AuthException;
import com.ksptool.assembly.entity.exception.BizException;
import com.ksptool.bio.biz.auth.model.auth.AuthUserSession;
import com.ksptool.bio.biz.auth.repository.GroupRepository;
import com.ksptool.bio.biz.auth.repository.UserGroupRepository;
import com.ksptool.bio.biz.auth.service.SessionService;
import com.ksptool.bio.biz.core.model.user.UserPo;
import com.ksptool.bio.biz.core.repository.OrgRepository;
import com.ksptool.bio.biz.core.repository.UserRepository;
import com.ksptool.bio.biz.qf.commons.LaunchParam;
import com.ksptool.bio.biz.qf.commons.QfVarsProc;
import com.ksptool.bio.biz.qf.commons.QfeVarsModel;
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
import java.util.*;
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
    private Flowable8NodeUtil flowable8NodeUtil;

    @Autowired
    private QfTodoRepository qfTodoRepository;

    @Autowired
    private UserRepository uRepository;

    @Autowired
    private UserGroupRepository ugRepository;

    @Autowired
    private GroupRepository gRepository;

    @Autowired
    private OrgRepository oRepository;


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
     * 按模型编码发起审批流程
     *
     * @param lp 启动流程参数
     * @return 流程实例ID
     * @throws BizException 业务异常
     */
    @Transactional(rollbackFor = Exception.class)
    public String launchProc(LaunchParam lp) throws BizException, AuthException {

        if (lp == null) {
            throw new BizException("启动流程失败,启动参数不能为空。");
        }

        var error = lp.validate();

        if (StringUtils.isNotBlank(error)) {
            throw new BizException(error);
        }

        //获取流程部署
        var deploy = qmdrRepository.getLatestActiveByCode(lp.getModelCode());

        if (deploy == null) {
            throw new BizException("启动流程失败,未找到可用的流程部署[code=" + lp.getModelCode() + "]");
        }

        //获取业务表单
        var form = qbfRepository.findById(deploy.getFormId())
                .orElseThrow(() -> new BizException("启动流程失败,业务表单不存在:[" + deploy.getFormId() + "]"));

        AuthUserSession aus = SessionService.session();
        var rid = aus.getRootId();
        var did = aus.getDeptId();
        var uid = aus.getUserId();
        var un = StringUtils.isNotBlank(aus.getNickname()) ? aus.getNickname() : aus.getUsername();

        if (rid == null || did == null || uid == null) {
            throw new BizException("启动流程失败,用户信息异常。");
        }

        //准备流程变量
        var pv = new HashMap<String, Object>();
        pv.put("initiator", uid.toString());
        pv.put(QfVarsProc.ROOT_ID.getValue(), rid);
        pv.put(QfVarsProc.DEPT_ID.getValue(), did);
        pv.put(QfVarsProc.INITIATOR_ID.getValue(), uid);
        pv.put(QfVarsProc.INITIATOR_NAME.getValue(), un);
        pv.put(QfVarsProc.INITIATOR_TIME.getValue(), LocalDateTime.now().format(dtf));
        pv.put(QfVarsProc.BIZ_FORM_ID.getValue(), form.getId());
        pv.put(QfVarsProc.TABLE_NAME.getValue(), form.getTableName());
        pv.put(QfVarsProc.DATA_ID.getValue(), lp.getDataId());

        //解析待办摘要
        var summary = un + "提交的" + form.getName() + "审批";
        var fst = form.getSummaryTemplate(); //Form Summary Template(FST)

        //如果业务表单使用了摘要模板 且 传入了 摘要模板参数，以摘要模板为准
        if (StringUtils.isNotBlank(fst) && !lp.getSParams().isEmpty()) {
            var pp = new PreparedPrompt(fst);
            for (var entry : lp.getSParams().entrySet()) {
                pp.setParameter(entry.getKey(), entry.getValue() != null ? entry.getValue() : "");
            }
            summary = pp.execute();
        }
        pv.put(QfVarsProc.SUMMARY.getValue(), summary);

        //设置发起人用户ID
        fiService.setAuthenticatedUserId(uid.toString());


        var m = new QfeBpmnModel().of(deploy.getBpmnXml());

        //----------------多实例节点处理 开始----------------

        //给多实例节点注入候选人 XML里面预设了一些多实例人员 需要在这里注入到PV 后面才可以在事件中拿到(flowable原生也依赖这些变量拿人)
        var uts = m.getUserTasks();
        var miUts = uts.stream().filter(QfeUserTask::isMultiInstance).toList();

        var mIds = new HashSet<Long>();
        var gIds = new HashSet<Long>();
        var oIds = new HashSet<Long>();

        //搜集所有MI节点的MIDS + GIDS + OIDS
        for (var miut : miUts) {

            //0:指定人 1:组 2:组织 3:发起人 10:任意人 (多实例不支持3和10)
            var kind = miut.getMemberKind();
            var memberIds = miut.getMemberIds();

            if (kind != MemberKind.USER && kind != MemberKind.GROUP && kind != MemberKind.DEPT) {
                throw new BizException("启动流程失败,多实例节点配置了不受支持的处理人类型:[" + kind + "]");
            }

            if (memberIds.isEmpty()) {
                throw new BizException("启动流程失败,多实例节点[" + miut.getId() + "]未配置候选人。");
            }

            //和前端约定好的变量名(qfMi_<节点ID>)
            var pvKey = "qfMi_" + miut.getId();

            if (kind == MemberKind.USER) {
                mIds.addAll(memberIds);
                pv.put(pvKey, memberIds);
            }

            if (kind == MemberKind.GROUP) {
                gIds.addAll(memberIds);
                pv.put(pvKey, memberIds);
            }

            if (kind == MemberKind.DEPT) {
                oIds.addAll(memberIds);
                pv.put(pvKey, memberIds);
            }

        }

        //对M+G+O IDS做后处理校验 防止前端或XML侧带入不合法或已删除的ID
        if (!mIds.isEmpty()) {
            if (uRepository.countByIds(new ArrayList<>(mIds)) != mIds.size()) {
                throw new BizException("启动流程失败,用户ID列表中包含不合法的ID。");
            }
        }
        if (!gIds.isEmpty()) {
            if (gRepository.countByIds(new ArrayList<>(gIds)) != gIds.size()) {
                throw new BizException("启动流程失败,用户组ID列表中包含不合法的ID。");
            }
        }
        if (!oIds.isEmpty()) {
            if (oRepository.countByIds(new ArrayList<>(oIds)) != oIds.size()) {
                throw new BizException("启动流程失败,组织机构ID列表中包含不合法的ID。");
            }
        }

        //----------------多实例节点处理 结束----------------


        //----------------发起时选人节点处理 开始----------------
        //给发起时选人的节点注入候选人 由业务方传入 
        var isUts = m.getUserTasks().stream().filter(QfeUserTask::isInitSelected).toList(); //先搜集所有"发起选人"的节点(isUts)

        //先做初筛 防止后面出问题 后面不做校验
        var isUtsSize = isUts.size();
        var lpMembersSize = lp.getMembers().size();
        if (isUtsSize != lpMembersSize) {
            throw new BizException("启动流程失败,有" + isUtsSize + "个节点需在发起时指定处理人,但业务方指定的人员数量不足或过多。");
        }

        for (var isUt : isUts) {

            //找出业务方传入的人
            var lpMemberId = lp.getMemberId(isUt.getId());

            if (lpMemberId == null || uRepository.countByIds(List.of(lpMemberId)) < 1) {
                throw new BizException("启动流程失败,节点[" + isUt.getId() + "]未能找到指定处理人或处理人不合法。");
            }

            //获取这个节点允许的选人范围 只有可能是 10:任意人 0:指定人 1:组 
            var rgr = isUt.getMemberKind();

            if (rgr != MemberKind.ANYONE && rgr != MemberKind.USER && rgr != MemberKind.GROUP) {
                throw new BizException("启动流程失败,节点[" + isUt.getId() + "]配置了不受支持的处理人类型:[" + rgr + "]");
            }

            //校验范围 前端一定会传范围 否则保存时会拦截
            if (rgr == MemberKind.USER) {

                var rgrMids = isUt.getMemberIds();

                if (!rgrMids.contains(lpMemberId)) {
                    throw new BizException("启动流程失败,节点[" + isUt.getId() + "] 所指定的处理人不合法。(用户范围超限)");
                }

            }

            //组范围校验 业务方传的是单用户ID 直接校验用户有没有这个组即可
            if (rgr == MemberKind.GROUP) {

                var rgrGids = isUt.getMemberIds();

                if (!ugRepository.hasAnyGroupsByUserId(lpMemberId, rgrGids)) {
                    throw new BizException("启动流程失败,节点[" + isUt.getId() + "] 所指定的处理人不合法。(组范围超限)");
                }

            }

            //所有校验通过 注入流程变量
            pv.put("qfAprNode_" + isUt.getId(), lpMemberId);
        }


        //var qfeModel = new QfeBpmnModel().of(frpService.getBpmnModel(deploy.getEngProcessDefId()));
        //if (qfeModel.getBpmnModel() == null || qfeModel.getBpmnModel().getMainProcess() == null) {
        //    throw new BizException("无法启动流程,未找到可用的流程模型[code=" + deploy.getCode() + "]");
        //}

        //prepareUserTaskVars(qfeModel.getUserTasks(), pv, null);

        try {
            ProcessInstance pi = frService.startProcessInstanceById(
                    deploy.getEngProcessDefId(),
                    lp.getDataId().toString(),
                    pv);

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
            Map<Long, String> idToName = uRepository.findAllById(allUserIds).stream()
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
        if (!waitingList.isEmpty()) {
            List<UserPo> userList = uRepository.findAllById(waitingList.stream().map(QfTodoPo::getMemberId).toList());
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
