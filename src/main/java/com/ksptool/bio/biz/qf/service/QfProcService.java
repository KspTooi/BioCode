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
import com.ksptool.bio.biz.qf.commons.qfe.QfeBpmnModel;
import com.ksptool.bio.biz.qf.commons.qfe.QfeUserTask;
import com.ksptool.bio.biz.qf.commons.qfe.QfeUserTask.MemberKind;
import com.ksptool.bio.biz.qf.commons.util.Flowable8NodeUtil;
import com.ksptool.bio.biz.qf.model.qftodo.QfTodoPo;
import com.ksptool.bio.biz.qf.model.qftodo.dto.GetProcessApproveFlowDto;
import com.ksptool.bio.biz.qf.model.qftodo.dto.GetProcessApproveFlowRecordDto;
import com.ksptool.bio.biz.qf.model.qftodo.vo.ApproveFlowRecordVo;
import com.ksptool.bio.biz.qf.model.qftodo.vo.GetProcNodeDefineVo;
import com.ksptool.bio.biz.qf.repository.QfBizFormRepository;
import com.ksptool.bio.biz.qf.repository.QfModelDeployRcdRepository;
import com.ksptool.bio.biz.qf.repository.QfTodoRepository;
import com.ksptool.text.PreparedPrompt;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.flowable.engine.IdentityService;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
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
@Slf4j
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

    @Autowired
    private TaskService ftService;


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

        log.info("[QF-Launcher] 发起时选人节点数量: {}, 传入的成员参数数量: {}", isUts.size(), lp.getMembers().size());
        log.info("[QF-Launcher] 传入的成员参数: {}", lp.getMembers());

        //先做初筛 防止后面出问题 后面不做校验
        var isUtsSize = isUts.size();
        var lpMembersSize = lp.getMembers().size();
        if (isUtsSize != lpMembersSize) {
            throw new BizException("启动流程失败,有" + isUtsSize + "个节点需在发起时指定处理人,但业务方指定的人员数量不足或过多。");
        }

        for (var isUt : isUts) {

            //找出业务方传入的人
            var lpMemberId = lp.getMemberId(isUt.getId());
            log.info("[QF-Launcher] 节点[{}] 获取到的成员ID: {}", isUt.getId(), lpMemberId);

            if (lpMemberId == null || uRepository.countByIds(List.of(lpMemberId)) < 1) {
                throw new BizException("启动流程失败,节点[" + isUt.getId() + "]未能找到指定处理人或处理人不合法。");
            }

            //获取这个节点允许的选人范围 只有可能是 10:任意人 0:指定人 1:组 
            var rgr = isUt.getMemberKind();
            log.info("[QF-Launcher] 节点[{}] 的成员类型: {}", isUt.getId(), rgr);

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
            String varKey = "qfAprNode_" + isUt.getId();
            pv.put(varKey, lpMemberId);
            log.info("[QF-Launcher] 注入流程变量: {} = {}", varKey, lpMemberId);
        }


        try {
            ProcessInstance pi = frService.startProcessInstanceById(
                    deploy.getEngProcessDefId(),
                    lp.getDataId().toString(),
                    pv);


            //跳过那些"首次发起时跳过节点" 
            var guard = 0;
            while (guard < 50) {
                guard++;
                var tasks = ftService.createTaskQuery()
                        .processInstanceId(pi.getId())
                        .active()
                        .list();
                if (tasks.isEmpty()) {
                    break;
                }
                var skipped = false;
                for (var task : tasks) {
                    var ut = m.getUserTask(task.getTaskDefinitionKey());
                    if (ut == null || !ut.isInitSkip()) {
                        continue;
                    }
                    ftService.complete(task.getId());
                    skipped = true;
                    log.info("[QF-Launcher]跳过'首次发起时跳过节点'[" + ut.getId() + "][" + ut.getName() + "]。");
                }
                if (!skipped) {
                    log.error("[QF-Launcher]未能跳过所有'首次发起时跳过节点'。");
                    break;
                }
            }

            return pi.getId();

        } finally {
            fiService.setAuthenticatedUserId(null);
        }

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
    public List<GetProcNodeDefineVo> getProcNodeDefine(String modelCode) throws BizException {

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

        List<GetProcNodeDefineVo> result = new ArrayList<>();
        // 按类型收集需要查询名称的节点信息（key=result index, value=member IDs）
        Map<Integer, List<Long>> userIdsToLookup = new HashMap<>();
        Map<Integer, List<Long>> groupIdsToLookup = new HashMap<>();
        Map<Integer, List<Long>> deptIdsToLookup = new HashMap<>();

        for (QfeUserTask ut : qfeModel.getUserTasksInFlowOrder()) {
            var vo = new GetProcNodeDefineVo();
            vo.setNodeId(ut.getId());
            vo.setNodeName(ut.getName());

            var memberKind = ut.getMemberKind();
            vo.setMemberKind(memberKind != null ? memberKind.getValue() : null);

            var approveKind = ut.getApproveKind();
            vo.setAprKind(approveKind != null ? approveKind.getValue() : 0);

            var memberIds = ut.getMemberIds();
            vo.setMemberIds(memberIds.stream().map(String::valueOf).toList());

            // 通过ID查询最新的名称，不从模型读取（模型中的名称可能过期）
            if (!memberIds.isEmpty()) {
                if (memberKind == MemberKind.USER) {
                    userIdsToLookup.put(result.size(), memberIds);
                } else if (memberKind == MemberKind.GROUP) {
                    groupIdsToLookup.put(result.size(), memberIds);
                } else if (memberKind == MemberKind.DEPT) {
                    deptIdsToLookup.put(result.size(), memberIds);
                }
            }
            vo.setMemberNames(List.of());

            result.add(vo);
        }

        // 批量查询用户名称
        if (!userIdsToLookup.isEmpty()) {
            var allUserIds = userIdsToLookup.values().stream()
                    .flatMap(List::stream)
                    .distinct()
                    .toList();
            Map<Long, String> idToName = uRepository.findAllById(allUserIds).stream()
                    .collect(Collectors.toMap(UserPo::getId, u -> StringUtils.isNotBlank(u.getNickname()) ? u.getNickname() : u.getUsername(), (a, b) -> a));
            for (var entry : userIdsToLookup.entrySet()) {
                var vo = result.get(entry.getKey());
                var names = entry.getValue().stream()
                        .map(id -> idToName.getOrDefault(id, String.valueOf(id)))
                        .toList();
                vo.setMemberNames(names);
            }
        }

        // 批量查询用户组名称
        if (!groupIdsToLookup.isEmpty()) {
            var allGroupIds = groupIdsToLookup.values().stream()
                    .flatMap(List::stream)
                    .distinct()
                    .toList();
            Map<Long, String> idToName = gRepository.getGroupsByIds(allGroupIds).stream()
                    .collect(Collectors.toMap(g -> g.getId(), g -> g.getName(), (a, b) -> a));
            for (var entry : groupIdsToLookup.entrySet()) {
                var vo = result.get(entry.getKey());
                var names = entry.getValue().stream()
                        .map(id -> idToName.getOrDefault(id, String.valueOf(id)))
                        .toList();
                vo.setMemberNames(names);
            }
        }

        // 批量查询组织名称
        if (!deptIdsToLookup.isEmpty()) {
            var allDeptIds = deptIdsToLookup.values().stream()
                    .flatMap(List::stream)
                    .distinct()
                    .toList();
            Map<Long, String> idToName = oRepository.getByIds(allDeptIds).stream()
                    .collect(Collectors.toMap(o -> o.getId(), o -> o.getName(), (a, b) -> a));
            for (var entry : deptIdsToLookup.entrySet()) {
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
        //从所有待办记录中收集需要查询的用户ID（包括已办 status=1，其 finMemberName 可能为空）
        Set<Long> needUserIds = new HashSet<>();
        for (var todo : todoList) {
            if (StringUtils.isBlank(todo.getFinMemberName())) {
                if (todo.getMemberId() != null) {
                    needUserIds.add(todo.getMemberId());
                }
                if (todo.getFinMemberId() != null) {
                    needUserIds.add(todo.getFinMemberId());
                }
            }
        }
        Map<Long, UserPo> userMap = needUserIds.isEmpty() ? new HashMap<>()
                : uRepository.findAllById(new ArrayList<>(needUserIds)).stream()
                .collect(Collectors.toMap(UserPo::getId, u -> u, (a, b) -> a));
        return todoList.stream().map(todo -> {
            ApproveFlowRecordVo vo = new ApproveFlowRecordVo();
            vo.setNodeName(todo.getNodeName());
            if (StringUtils.isNotBlank(todo.getFinMemberName())) {
                vo.setFinMemberName(todo.getFinMemberName());
            } else {
                var userPo = userMap.get(todo.getMemberId());
                vo.setFinMemberName(userPo == null ? null : (StringUtils.isNotBlank(userPo.getNickname()) ? userPo.getNickname() : userPo.getUsername()));
            }
            vo.setFinTime(todo.getFinTime());
            vo.setAction(todo.getAction());
            vo.setComment(todo.getComment());
            vo.setStatus(todo.getStatus());
            return vo;
        }).toList();
    }
}
