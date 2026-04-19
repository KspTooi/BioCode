package com.ksptool.bio.biz.qf.service;

import com.ksptool.assembly.entity.exception.AuthException;
import com.ksptool.assembly.entity.exception.BizException;
import com.ksptool.bio.biz.auth.service.SessionService;
import com.ksptool.bio.biz.qf.commons.QfModelTools;
import com.ksptool.bio.biz.qf.commons.QfVarsProc;
import com.ksptool.bio.biz.qf.model.qfmodeldeployrcd.QfModelDeployRcdPo;
import com.ksptool.bio.biz.qf.repository.QfBizFormRepository;
import com.ksptool.bio.biz.qf.repository.QfModelDeployRcdRepository;
import org.apache.commons.lang3.StringUtils;
import org.flowable.bpmn.model.UserTask;
import org.flowable.engine.IdentityService;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.runtime.ProcessInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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


    /**
     * 按模型编码发起审批流程(测试)
     *
     * @param modelCode   模型编码
     * @param bizFormCode 业务表单编码
     * @param dataId      业务数据ID
     * @return 流程实例ID
     * @throws BizException 业务异常
     */
    @Transactional(rollbackFor = Exception.class)
    public String launchProc(String modelCode, String bizFormCode, Long dataId) throws BizException, AuthException {

        if (StringUtils.isBlank(modelCode)) {
            throw new BizException("无法启动流程,模型编码不能为空");
        }

        if (StringUtils.isBlank(bizFormCode)) {
            throw new BizException("无法启动流程,业务表单编码不能为空");
        }

        if (dataId == null) {
            throw new BizException("无法启动流程,业务数据ID不能为空");
        }

        //查询部署和表单
        var deploy = qmdrRepository.getLatestActiveByCode(modelCode);
        var form = qbfRepository.getActiveByCode(bizFormCode);

        if (deploy == null) {
            throw new BizException("无法启动流程,未找到可用的流程部署[code=" + modelCode + "]");
        }

        if (form == null) {
            throw new BizException("无法启动流程,未找到可用的业务表单[code=" + bizFormCode + "]");
        }

        //获取当前用户的rid、did、uid
        var aud = SessionService.session();
        var rootId = aud.getRootId();
        var deptId = aud.getDeptId();
        var userId = aud.getId();
        var nickname = aud.getNickname();

        if (rootId == null || deptId == null) {
            throw new BizException("无法启动流程,未能获取到有效的租户ID或者部门ID！");
        }

        if (userId == null) {
            throw new BizException("无法启动流程,未能获取到有效的用户ID！");
        }

        //如果用户没有昵称，用用户名(账号)代替。
        if (StringUtils.isBlank(nickname)) {
            nickname = aud.getUsername();
        }

        //准备流程参数
        var p = new HashMap<String, Object>();

        //准备发起方数据
        p.put(QfVarsProc.ROOT_ID + "", rootId);
        p.put(QfVarsProc.DEPT_ID + "", deptId);
        p.put(QfVarsProc.INITIATOR_ID + "", userId);
        p.put(QfVarsProc.INITIATOR_NAME + "", nickname);
        p.put(QfVarsProc.INITIATOR_TIME + "", LocalDateTime.now().format(dtf));

        //准备业务数据
        p.put(QfVarsProc.BIZ_FORM_ID + "", form.getId());
        p.put(QfVarsProc.TABLE_NAME + "", form.getTableName());
        p.put(QfVarsProc.DATA_ID + "", dataId);
        p.put(QfVarsProc.SUMMARY + "", nickname + "提交的" + form.getName() + "审批");

        //设置流程发起人
        fiService.setAuthenticatedUserId(userId.toString());

        //注入办理成员
        injectMembers(deploy, p);

        try {
            //启动流程
            ProcessInstance pi = frService.startProcessInstanceById(
                    deploy.getEngProcessDefId(),
                    dataId.toString(),
                    p);

            return pi.getId();

        } finally {

            /*
             * setAuthenticatedUserId 内部用的是 ThreadLocal。因为 Tomcat 处理请求用的通常是线程池，如果这一次请求执行完没有把它设为 null 清除掉，那么下一个用户的请求如果不巧复用了这个线程，引擎会以为下一个业务流程还是前一个用户发起的！
             * #2026-04-17测试结果 这个坑主要是因为流程引擎也会自动记录是谁完成的任务，如果下一个线程不巧因为异常或者别的原因没有setAuthenticatedUserId 就会有数据问题。
             */
            fiService.setAuthenticatedUserId(null);
        }

    }


    /**
     * 注入办理成员,把BPMN中的多实例节点配置的候选人注入到流程参数中
     * <p>
     * 这里主要是处理多实例任务的候选人注入
     * 1.对于单实例任务，前端会把办理人存成
     * assignee="101" 或 candidateUsers="101,102"
     * Flowable 引擎启动时能原生读取这些写死在 XML 里的属性并直接分配任务，所以不需要后端额外干预。
     * <p>
     * 2.对于多实例任务，前端会在XML节点里面放入一些东西，这里分几种情况
     * 多实例 - 指定用户
     * candidateUsers = "101,102"
     * assigneeKind = "user"
     * collection = "${assigneeList}" 特别注意: 这里就是引擎运行时会从流程参数中获取的变量名
     * <p>
     * 多实例 - 用户组
     * candidateGroups = "101,102"
     * assigneeKind = "group"
     * collection = "${groupList}"
     * <p>
     * 多实例 - 组织机构 (部门)
     * candidateGroups = "101,102"
     * assigneeKind = "dept"
     * collection = "${groupList}"
     * <p>
     * 所以这里我就要把这些candidateUsers或者candidateGroups的值提出来变成List，然后吐到流程参数里。
     * <p>
     * 但是在吐之前，还要说明一下，这里不能直接喷吐，因为一个模型中有可能会有多个多实例节点
     * 所以还需要借助QfMiRenameParseHandler(QMRPH)来处理，
     * QMRPH会把collection="${assigneeList}" 改写为 collection="${qfMi_<taskId>}"
     * <p>
     * 所以最终吐出到流程参数里面的数据大概是这样
     * qfMi_ut_1 = List<String> = [101,102]
     * qfMi_ut_2 = List<String> = [101,102]
     * qfMi_ut_3 = List<String> = [101,102]
     * ...
     * <p>
     * 这样引擎在运行时，就会根据这些List来循环生成子任务。
     * :: 这恶心的流程引擎，原本以为会是精装房，结果是一堆水泥、砖块和钢筋，别说自己装修，连房子都要自己盖。🤣🤣🤣
     *
     * @param deploy 流程部署
     * @param p      流程参数（将会在此 Map 中放入各个多实例节点的候选人 List）
     * @throws BizException 业务异常
     */
    private void injectMembers(QfModelDeployRcdPo deploy, Map<String, Object> p) throws BizException {

        if (deploy == null || p == null) {
            throw new BizException("无法注入办理成员,流程部署或流程参数不能为空");
        }

        //获取流程模型
        var model = frpService.getBpmnModel(deploy.getEngProcessDefId());

        if (model == null) {
            throw new BizException("无法注入办理成员,未找到可用的流程模型[code=" + deploy.getCode() + "]");
        }

        //获取主流程
        var mainProc = model.getMainProcess();

        if (mainProc == null) {
            throw new BizException("无法注入办理成员,BPMN主流程不存在[code=" + deploy.getCode() + "]");
        }

        //获取所有用户任务
        var userTasks = mainProc.findFlowElementsOfType(UserTask.class, true);

        for (UserTask ut : userTasks) {

            var loop = ut.getLoopCharacteristics();

            if (loop == null) {
                //非多实例节点，跳过
                continue;
            }

            var coll = loop.getInputDataItem();   // 对应 BPMN 中 collection="${xxx}"
            if (StringUtils.isBlank(coll)) {
                //没有配 collection 的多实例(通过 loopCardinality 写死 N 次), 跳过
                continue;
            }

            //提取 ${assigneeList} 中的变量名 assigneeList
            var varName = QfModelTools.extractElVarName(coll);
            if (StringUtils.isBlank(varName)) {
                continue;
            }

            //调用方已预置则不覆盖（支持动态候选场景）
            if (p.containsKey(varName)) {
                continue;
            }

            //按节点类型决定数据源: 指定用户 → candidateUsers, 指定用户组/部门 → candidateGroups
            List<String> values = QfModelTools.resolveCandidates(ut);

            if (values.isEmpty()) {
                //空集合会导致多实例直接跳过节点
                throw new BizException("多实例节点[" + ut.getId() + "]未配置候选人");
            }

            p.put(varName, values);
        }

    }


}
