package com.ksptool.bio.biz.qf.service;

import com.ksptool.assembly.entity.exception.BizException;
import com.ksptool.assembly.entity.web.CommonIdDto;
import com.ksptool.assembly.entity.web.PageResult;
import com.ksptool.bio.biz.qf.commons.QfModelTools;
import com.ksptool.bio.biz.qf.model.qfmodel.QfModelPo;
import com.ksptool.bio.biz.qf.model.qfmodel.dto.AddQfModelDto;
import com.ksptool.bio.biz.qf.model.qfmodel.dto.DesignQfModelDto;
import com.ksptool.bio.biz.qf.model.qfmodel.dto.EditQfModelDto;
import com.ksptool.bio.biz.qf.model.qfmodel.dto.GetQfModelListDto;
import com.ksptool.bio.biz.qf.model.qfmodel.vo.GetQfModelDetailsVo;
import com.ksptool.bio.biz.qf.model.qfmodel.vo.GetQfModelListVo;
import com.ksptool.bio.biz.qf.model.qfmodeldeployrcd.QfModelDeployRcdPo;
import com.ksptool.bio.biz.qf.model.qfmodelgroup.QfModelGroupPo;
import com.ksptool.bio.biz.qf.repository.QfModelDeployRcdRepository;
import com.ksptool.bio.biz.qf.repository.QfModelGroupRepository;
import com.ksptool.bio.biz.qf.repository.QfModelRepository;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.repository.Deployment;
import org.flowable.engine.repository.ProcessDefinition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;

import static com.ksptool.entities.Entities.as;
import static com.ksptool.entities.Entities.assign;


/**
 * 流程模型服务
 * 
 * @author KspTool
 * @since 2026-04-17
 */
@Service
public class QfModelService {

    @Autowired
    private RepositoryService flowRepositoryService;

    @Autowired
    private QfModelRepository repository;

    @Autowired
    private QfModelGroupRepository qfModelGroupRepository;

    @Autowired
    private QfModelDeployRcdRepository qfModelDeployRcdRepository;

    /**
     * 查询流程模型列表
     *
     * @param dto 查询条件
     * @return 查询结果
     */
    public PageResult<GetQfModelListVo> getQfModelList(GetQfModelListDto dto) {
        Page<GetQfModelListVo> page = repository.getQfModelList(dto, dto.pageRequest());
        if (page.isEmpty()) {
            return PageResult.successWithEmpty();
        }
        return PageResult.success(page.getContent(), (int) page.getTotalElements());
    }

    /**
     * 创建新版本流程模型
     *
     * @param dto 创建新版本条件
     * @throws BizException 业务异常
     */
    @Transactional(rollbackFor = Exception.class)
    public void createNewVersionQfModel(CommonIdDto dto) throws BizException {
        QfModelPo po = repository.findById(dto.getId())
                .orElseThrow(() -> new BizException("创建新版本失败,数据不存在或无权限访问."));

        //只有已部署状态才能创建新版本
        if (po.getStatus() != 1) {
            throw new BizException("创建新版本失败,只有已部署状态才能创建新版本。");
        }

        QfModelPo newVersionPo = as(po, QfModelPo.class);
        newVersionPo.setId(null);
        newVersionPo.setStatus(0); //新版本状态为草稿
        newVersionPo.setVersion(po.getVersion() + 1);

        //旧版本置为历史
        po.setStatus(2);

        //保存新版本
        repository.save(newVersionPo);

        //保存旧版本
        repository.save(po);
    }

    /**
     * 新增流程模型
     *
     * @param dto 新增条件
     */
    @Transactional(rollbackFor = Exception.class)
    public void addQfModel(AddQfModelDto dto) throws BizException {

        //查询编码是否被占用
        var existPo = repository.countByCodeExcludeId(dto.getCode(), null);
        if (existPo > 0) {
            throw new BizException("流程模型编码已存在:[" + dto.getCode() + "]");
        }

        QfModelPo insertPo = as(dto, QfModelPo.class);

        //设置状态和版本号
        insertPo.setStatus(0);
        insertPo.setVersion(1);

        //处理模型分组
        if (dto.getGroupId() != null) {
            QfModelGroupPo groupPo = qfModelGroupRepository.findById(dto.getGroupId())
                    .orElseThrow(() -> new BizException("模型分组不存在:[" + dto.getGroupId() + "]"));
            insertPo.setGroupId(groupPo.getId());
        }

        repository.save(insertPo);
    }

    /**
     * 编辑流程模型
     *
     * @param dto 编辑条件
     * @throws BizException 业务异常
     */
    @Transactional(rollbackFor = Exception.class)
    public void editQfModel(EditQfModelDto dto) throws BizException {

        QfModelPo updatePo = repository.findById(dto.getId())
                .orElseThrow(() -> new BizException("更新失败,数据不存在或无权限访问."));

        //只有草稿状态才能编辑
        if (updatePo.getStatus() != 0) {
            throw new BizException("编辑失败,只有草稿状态才能编辑。");
        }

        assign(dto, updatePo);

        //处理模型分组
        if (dto.getGroupId() != null) {
            QfModelGroupPo groupPo = qfModelGroupRepository.findById(dto.getGroupId())
                    .orElseThrow(() -> new BizException("模型分组不存在:[" + dto.getGroupId() + "]"));
            updatePo.setGroupId(groupPo.getId());
        }

        repository.save(updatePo);
    }

    /**
     * 设计流程模型BPMN
     *
     * @param dto 设计条件
     * @throws BizException 业务异常
     */
    @Transactional(rollbackFor = Exception.class)
    public void designQfModel(DesignQfModelDto dto) throws BizException {
        QfModelPo updatePo = repository.findById(dto.getId())
                .orElseThrow(() -> new BizException("设计失败,数据不存在或无权限访问."));

        //只有草稿状态才能设计
        if (updatePo.getStatus() != 0) {
            throw new BizException("设计失败,只有草稿状态才能设计。");
        }

        //先保存原始BPMN XML
        updatePo.setBpmnXml(dto.getBpmnXml());

        //如果BPMN XML有效，则格式化
        if (QfModelTools.isBpmnXmlValid(dto.getBpmnXml())) {
            updatePo.setBpmnXml(QfModelTools.formatBpmnXml(dto.getBpmnXml(), "qf_model_" + updatePo.getId(), updatePo.getName()));
        }

        repository.save(updatePo);
    }

    /**
     * 查询流程模型详情
     *
     * @param dto 查询条件
     * @return 查询结果
     * @throws BizException 业务异常
     */
    public GetQfModelDetailsVo getQfModelDetails(CommonIdDto dto) throws BizException {
        QfModelPo po = repository.findById(dto.getId())
                .orElseThrow(() -> new BizException("查询详情失败,数据不存在或无权限访问."));
        return as(po, GetQfModelDetailsVo.class);
    }

    /**
     * 删除流程模型
     *
     * @param dto 删除条件
     * @throws BizException 业务异常
     */
    @Transactional(rollbackFor = Exception.class)
    public void removeQfModel(CommonIdDto dto) throws BizException {

        //不支持批量删除 已在接口层校验
        if (dto.isBatch()) {
            repository.deleteAllById(dto.getIds());
            return;
        }

        //是否有部署物
        if (repository.countDeployByModelId(dto.getId()) > 0) {
            throw new BizException("删除失败,该模型有活跃的部署,请先删除部署。");
        }

        //删除模型
        repository.deleteById(dto.getId());
    }

    /**
     * 部署流程模型
     *
     * @param dto 部署条件
     * @throws BizException 业务异常
     */
    @Transactional(rollbackFor = Exception.class)
    public void deployQfModel(CommonIdDto dto) throws BizException {

        QfModelPo po = repository.findById(dto.getId())
                .orElseThrow(() -> new BizException("部署失败,数据不存在或无权限访问."));

        //不能在历史版本上部署
        if (po.getStatus() == 2) {
            throw new BizException("部署失败,模型状态异常。");
        }

        //是否有活跃的部署
        if (repository.countDeployByModelId(dto.getId()) > 0) {
            throw new BizException("部署失败,该模型已被部署。");
        }

        //校验BPMN XML是否有效
        if (!QfModelTools.isBpmnXmlValid(po.getBpmnXml())) {
            throw new BizException("部署失败,BPMN模型无效或设计错误。");
        }

        var category = "default";

        //如果模型有分组，则使用分组名称
        if (po.getGroupId() != null) {
            QfModelGroupPo groupPo = qfModelGroupRepository.findById(po.getGroupId())
                    .orElseThrow(() -> new BizException("模型分组不存在:[" + po.getGroupId() + "]"));
            category = groupPo.getName();
        }

        var processName = "qf_model_" + po.getId() + "_" + po.getVersion() + ".bpmn";

        //部署流程模型
        Deployment deployment = flowRepositoryService.createDeployment()
                .name("定义:" + po.getName())
                .key(po.getCode())
                .category(category)
                .addBytes(processName, po.getBpmnXml().getBytes(StandardCharsets.UTF_8))
                .deploy();

        //查询部署的流程定义
        ProcessDefinition processDefinition = flowRepositoryService
                .createProcessDefinitionQuery()
                .deploymentId(deployment.getId())
                .singleResult();

        if (processDefinition == null) {
            // 抛出异常后，Spring 会把刚才 Flowable 的 deploy() 操作自动撤销（回滚），保持数据库干净！
            throw new BizException("部署失败: Flowable 引擎未能成功解析模型文件，请检查模型内容是否规范。");
        }

        //到这里说明万无一失了，开始创建一条部署记录
        var rcd = new QfModelDeployRcdPo();
        rcd.setRootId(po.getRootId());
        rcd.setDeptId(po.getDeptId());
        rcd.setModelId(po.getId());
        rcd.setName(po.getName());
        rcd.setCode(po.getCode());
        rcd.setBpmnXml(po.getBpmnXml());
        rcd.setVersion(po.getVersion());
        rcd.setEngDeploymentId(deployment.getId());
        rcd.setEngProcessDefId(processDefinition.getId());
        rcd.setEngDeployResult("部署成功");
        rcd.setStatus(0);

        //保存部署记录
        qfModelDeployRcdRepository.save(rcd);

        //更新模型状态为已部署
        po.setStatus(1);
        repository.save(po);
    }

}
