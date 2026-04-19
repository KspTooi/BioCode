package com.ksptool.bio.biz.qf.service;

import com.ksptool.assembly.entity.exception.BizException;
import com.ksptool.assembly.entity.web.CommonIdDto;
import com.ksptool.assembly.entity.web.PageResult;
import com.ksptool.bio.biz.qf.model.qfmodelgroup.QfModelGroupPo;
import com.ksptool.bio.biz.qf.model.qfmodelgroup.dto.AddQfModelGroupDto;
import com.ksptool.bio.biz.qf.model.qfmodelgroup.dto.EditQfModelGroupDto;
import com.ksptool.bio.biz.qf.model.qfmodelgroup.dto.GetQfModelGroupListDto;
import com.ksptool.bio.biz.qf.model.qfmodelgroup.vo.GetQfModelGroupDetailsVo;
import com.ksptool.bio.biz.qf.model.qfmodelgroup.vo.GetQfModelGroupListVo;
import com.ksptool.bio.biz.qf.repository.QfModelGroupRepository;
import com.ksptool.bio.biz.qf.repository.QfModelRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.ksptool.entities.Entities.as;
import static com.ksptool.entities.Entities.assign;


/**
 * 流程模型分组服务
 *
 * @author KspTool(ksptool@outlook.com)
 * @license Proprietary
 * 版权所有 (c) 2026 KspTool及其贡献者保留所有权利。
 * 未经事先书面许可，严禁任何形式的复制或分发。
 * @since 2026-04-17
 */
@Service
public class QfModelGroupService {

    @Autowired
    private QfModelGroupRepository repository;

    @Autowired
    private QfModelRepository modelRepository;

    /**
     * 查询流程模型分组列表
     *
     * @param dto 查询条件
     * @return 查询结果
     */
    public PageResult<GetQfModelGroupListVo> getQfModelGroupList(GetQfModelGroupListDto dto) {
        QfModelGroupPo query = new QfModelGroupPo();
        assign(dto, query);

        Page<QfModelGroupPo> page = repository.getQfModelGroupList(query, dto.pageRequest());
        if (page.isEmpty()) {
            return PageResult.successWithEmpty();
        }

        List<GetQfModelGroupListVo> vos = as(page.getContent(), GetQfModelGroupListVo.class);
        return PageResult.success(vos, (int) page.getTotalElements());
    }

    /**
     * 新增流程模型分组
     *
     * @param dto 新增条件
     */
    @Transactional(rollbackFor = Exception.class)
    public void addQfModelGroup(AddQfModelGroupDto dto) throws BizException {

        //查询编码是否被占用
        var existPo = repository.countByCodeExcludeId(dto.getCode(), null);

        if (existPo > 0) {
            throw new BizException("流程模型分组编码已存在:[" + dto.getCode() + "]");
        }

        QfModelGroupPo insertPo = as(dto, QfModelGroupPo.class);
        repository.save(insertPo);
    }

    /**
     * 编辑流程模型分组
     *
     * @param dto 编辑条件
     * @throws BizException 业务异常
     */
    @Transactional(rollbackFor = Exception.class)
    public void editQfModelGroup(EditQfModelGroupDto dto) throws BizException {

        //查询编码是否被占用
        var existPo = repository.countByCodeExcludeId(dto.getCode(), dto.getId());
        if (existPo > 0) {
            throw new BizException("流程模型分组编码已存在:[" + dto.getCode() + "]");
        }

        QfModelGroupPo updatePo = repository.findById(dto.getId())
                .orElseThrow(() -> new BizException("更新失败,数据不存在或无权限访问."));

        assign(dto, updatePo);
        repository.save(updatePo);
    }

    /**
     * 查询流程模型分组详情
     *
     * @param dto 查询条件
     * @return 查询结果
     * @throws BizException 业务异常
     */
    public GetQfModelGroupDetailsVo getQfModelGroupDetails(CommonIdDto dto) throws BizException {
        QfModelGroupPo po = repository.findById(dto.getId())
                .orElseThrow(() -> new BizException("查询详情失败,数据不存在或无权限访问."));
        return as(po, GetQfModelGroupDetailsVo.class);
    }

    /**
     * 删除流程模型分组
     *
     * @param dto 删除条件
     * @throws BizException 业务异常
     */
    @Transactional(rollbackFor = Exception.class)
    public void removeQfModelGroup(CommonIdDto dto) throws BizException {

        if (modelRepository.countByGroupId(dto.getId()) > 0) {
            throw new BizException("删除失败,该模型分组下有流程模型,请先移除流程模型后再删除.");
        }

        repository.deleteById(dto.getId());
    }

}
