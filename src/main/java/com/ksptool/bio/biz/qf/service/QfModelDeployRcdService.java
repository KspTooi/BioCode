package com.ksptool.bio.biz.qf.service;

import com.ksptool.assembly.entity.exception.BizException;
import com.ksptool.assembly.entity.web.CommonIdDto;
import com.ksptool.assembly.entity.web.PageResult;
import com.ksptool.bio.biz.qf.model.qfmodel.QfModelPo;
import com.ksptool.bio.biz.qf.model.qfmodeldeployrcd.QfModelDeployRcdPo;
import com.ksptool.bio.biz.qf.model.qfmodeldeployrcd.dto.GetQfModelDeployRcdListDto;
import com.ksptool.bio.biz.qf.model.qfmodeldeployrcd.vo.GetQfModelDeployRcdDetailsVo;
import com.ksptool.bio.biz.qf.model.qfmodeldeployrcd.vo.GetQfModelDeployRcdListVo;
import com.ksptool.bio.biz.qf.repository.QfModelDeployRcdRepository;
import com.ksptool.bio.biz.qf.repository.QfModelRepository;
import jakarta.persistence.Tuple;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.RuntimeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.ksptool.entities.Entities.as;
import static com.ksptool.entities.Entities.assign;
import static com.ksptool.bio.biz.core.common.TupleMapper.tupleAs;


/**
 * 流程模型部署历史服务
 *
 * @author KspTool(ksptool@outlook.com)
 * @license Proprietary
 * 版权所有 (c) 2026 KspTool及其贡献者保留所有权利。
 * 未经事先书面许可，严禁任何形式的复制或分发。
 * @since 2026-04-17
 */
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

        Page<Tuple> page = repository.getQfModelDeployRcdList(query, dto.pageRequest());
        if (page.isEmpty()) {
            return PageResult.successWithEmpty();
        }

        Page<GetQfModelDeployRcdListVo> voPage = tupleAs(page, GetQfModelDeployRcdListVo.class);
        return PageResult.success(voPage.getContent(), (int) voPage.getTotalElements());
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


}
