package com.ksptool.bio.biz.qf.service;

import com.ksptool.assembly.entity.exception.AuthException;
import com.ksptool.assembly.entity.exception.BizException;
import com.ksptool.assembly.entity.web.CommonIdDto;
import com.ksptool.assembly.entity.web.PageResult;
import com.ksptool.bio.biz.auth.service.SessionService;
import com.ksptool.bio.biz.qf.commons.QfProcVars;
import com.ksptool.bio.biz.qf.model.qfmodel.QfModelPo;
import com.ksptool.bio.biz.qf.model.qfmodeldeployrcd.QfModelDeployRcdPo;
import com.ksptool.bio.biz.qf.model.qfmodeldeployrcd.dto.GetQfModelDeployRcdListDto;
import com.ksptool.bio.biz.qf.model.qfmodeldeployrcd.dto.LaunchQfProcessDto;
import com.ksptool.bio.biz.qf.model.qfmodeldeployrcd.vo.GetQfModelDeployRcdDetailsVo;
import com.ksptool.bio.biz.qf.model.qfmodeldeployrcd.vo.GetQfModelDeployRcdListVo;
import com.ksptool.bio.biz.qf.repository.QfModelDeployRcdRepository;
import com.ksptool.bio.biz.qf.repository.QfModelRepository;
import org.apache.commons.lang3.StringUtils;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.runtime.ProcessInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.ksptool.entities.Entities.as;
import static com.ksptool.entities.Entities.assign;


@Service
public class QfModelDeployRcdService {

    @Autowired
    private RepositoryService flowRepositoryService;

    @Autowired
    private RuntimeService flowRuntimeService;

    @Autowired
    private QfModelRepository qfModelRepository;

    @Autowired
    private QfModelDeployRcdRepository repository;

    /**
     * 查询流程模型部署历史列表
     *
     * @param dto 查询条件
     * @return 查询结果
     */
    public PageResult<GetQfModelDeployRcdListVo> getQfModelDeployRcdList(GetQfModelDeployRcdListDto dto) {
        QfModelDeployRcdPo query = new QfModelDeployRcdPo();
        assign(dto, query);

        Page<QfModelDeployRcdPo> page = repository.getQfModelDeployRcdList(query, dto.pageRequest());
        if (page.isEmpty()) {
            return PageResult.successWithEmpty();
        }

        List<GetQfModelDeployRcdListVo> vos = as(page.getContent(), GetQfModelDeployRcdListVo.class);
        return PageResult.success(vos, (int) page.getTotalElements());
    }


    /**
     * 查询流程模型部署历史详情
     *
     * @param dto 查询条件
     * @return 查询结果
     * @throws BizException 业务异常
     */
    public GetQfModelDeployRcdDetailsVo getQfModelDeployRcdDetails(CommonIdDto dto) throws BizException {
        QfModelDeployRcdPo po = repository.findById(dto.getId())
                .orElseThrow(() -> new BizException("查询详情失败,数据不存在或无权限访问."));
        return as(po, GetQfModelDeployRcdDetailsVo.class);
    }

    /**
     * 删除流程模型部署历史
     *
     * @param dto 删除条件
     * @throws BizException 业务异常
     */
    @Transactional(rollbackFor = Exception.class)
    public void removeQfModelDeployRcd(CommonIdDto dto) throws BizException {

        if (dto.isBatch()) {
            throw new BizException("不支持批量删除。");
        }

        QfModelDeployRcdPo po = repository.findById(dto.getId())
                .orElseThrow(() -> new BizException("删除失败,数据不存在或无权限访问."));

        // 检查是否存在运行中的流程实例
        if (po.getEngProcessDefId() != null && !po.getEngProcessDefId().isBlank()) {
            long runningCount = flowRuntimeService.createProcessInstanceQuery()
                    .processDefinitionId(po.getEngProcessDefId())
                    .count();
            if (runningCount > 0) {
                throw new BizException("删除失败,部署记录[" + po.getName() + "]下仍有" + runningCount + "个运行中的流程实例。");
            }
        }

        flowRepositoryService.deleteDeployment(po.getEngDeploymentId(), false);
        repository.deleteById(dto.getId());


        QfModelPo modelPo = qfModelRepository.findById(po.getModelId())
                .orElseThrow(() -> new BizException("模型不存在:[" + po.getModelId() + "]"));

        //如果模型状态是已部署，则恢复为草稿 (如果已经是历史了，则不恢复)
        if (modelPo.getStatus() == 1) {
            modelPo.setStatus(0);
        }

        qfModelRepository.save(modelPo);
    }


    /**
     * 挂起流程模型部署
     *
     * @param dto 挂起条件
     * @throws BizException 业务异常
     */
    @Transactional(rollbackFor = Exception.class)
    public void suspendQfModelDeployRcd(CommonIdDto dto) throws BizException {
        QfModelDeployRcdPo updatePo = repository.findById(dto.getId())
                .orElseThrow(() -> new BizException("挂起失败,数据不存在或无权限访问."));

        //只有正常状态才能挂起
        if (updatePo.getStatus() != 0) {
            throw new BizException("挂起失败,只有正常状态才能挂起。");
        }

        if (updatePo.getEngProcessDefId() == null || updatePo.getEngProcessDefId().isBlank()) {
            throw new BizException("挂起失败,该部署记录无有效的流程定义。");
        }

        // 挂起Flowable引擎中的流程定义，同时挂起关联的流程实例
        flowRepositoryService.suspendProcessDefinitionById(updatePo.getEngProcessDefId(), true, null);


        //更新为已挂起状态
        updatePo.setStatus(2);

        repository.save(updatePo);
    }

    /**
     * 恢复流程模型部署
     *
     * @param dto 恢复条件
     * @throws BizException 业务异常
     */
    @Transactional(rollbackFor = Exception.class)
    public void activateQfModelDeployRcd(CommonIdDto dto) throws BizException {
        QfModelDeployRcdPo updatePo = repository.findById(dto.getId())
                .orElseThrow(() -> new BizException("恢复失败,数据不存在或无权限访问."));

        if (updatePo.getStatus() != 2) {
            throw new BizException("恢复失败,只有已挂起状态才能恢复。");
        }

        if (updatePo.getEngProcessDefId() == null || updatePo.getEngProcessDefId().isBlank()) {
            throw new BizException("恢复失败,该部署记录无有效的流程定义。");
        }

        // 激活Flowable引擎中的流程定义，同时激活关联的流程实例
        flowRepositoryService.activateProcessDefinitionById(updatePo.getEngProcessDefId(), true, null);

        updatePo.setStatus(0);
        repository.save(updatePo);
    }

    /**
     * 按模型编码发起审批流程(测试)
     *
     * @param dto 发起参数
     * @return 流程实例ID
     * @throws BizException 业务异常
     */
    @Transactional(rollbackFor = Exception.class)
    public String launchQfProcess(LaunchQfProcessDto dto) throws BizException, AuthException {

        if (StringUtils.isBlank(dto.getCode())) {
            throw new BizException("模型编码不能为空");
        }

        List<QfModelDeployRcdPo> list = repository.findLatestActiveByCode(dto.getCode(), PageRequest.of(0, 1));
        if (list == null || list.isEmpty()) {
            throw new BizException("未找到可用的流程部署[code=" + dto.getCode() + "]");
        }

        QfModelDeployRcdPo deploy = list.get(0);
        if (StringUtils.isBlank(deploy.getEngProcessDefId())) {
            throw new BizException("部署记录无有效流程定义");
        }

        //合并流程变量: 调用方自定义变量 + QF域约定的业务/发起人变量(测试阶段先用假数据)
        //rootId/deptId 必须通过变量带入, 否则 QfTaskCreatedListener 脱离Session上下文无法回填
        Map<String, Object> merged = new HashMap<>();
        Map<String, Object> dtoVars = dto.getVariables();
        if (dtoVars != null) {
            merged.putAll(dtoVars);
        }
        var session = SessionService.session();
        merged.put(QfProcVars.BIZ_FORM_ID, 0L);
        merged.put(QfProcVars.TABLE_NAME, "test_table");
        merged.put(QfProcVars.DATA_ID, 0L);
        merged.put(QfProcVars.SUMMARY, "测试流程发起[code=" + dto.getCode() + "]");
        merged.put(QfProcVars.INITIATOR_ID, session.getUserId());
        //TODO 待 AuthUserDetails/Session 提供昵称字段后替换, 目前先用 userId 占位
        merged.put(QfProcVars.INITIATOR_NAME, String.valueOf(session.getUserId()));
        merged.put(QfProcVars.INITIATOR_TIME, LocalDateTime.now());
        merged.put(QfProcVars.ROOT_ID, session.getRootId());
        merged.put(QfProcVars.DEPT_ID, session.getDeptId());

        ProcessInstance pi = flowRuntimeService.startProcessInstanceById(
                deploy.getEngProcessDefId(),
                dto.getBusinessKey(),
                merged
        );
        return pi.getId();
    }
}
