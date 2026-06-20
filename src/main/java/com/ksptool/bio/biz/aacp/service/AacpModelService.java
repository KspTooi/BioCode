package com.ksptool.bio.biz.aacp.service;

import com.ksptool.assembly.entity.exception.BizException;
import com.ksptool.assembly.entity.web.CommonIdDto;
import com.ksptool.assembly.entity.web.PageResult;
import com.ksptool.bio.biz.aacp.model.model.AacpModelPo;
import com.ksptool.bio.biz.aacp.model.model.dto.AddModelDto;
import com.ksptool.bio.biz.aacp.model.model.dto.EditModelDto;
import com.ksptool.bio.biz.aacp.model.model.dto.GetModelListDto;
import com.ksptool.bio.biz.aacp.model.model.vo.GetModelDetailsVo;
import com.ksptool.bio.biz.aacp.model.model.vo.GetModelListVo;
import com.ksptool.bio.biz.aacp.repository.ModelRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.ksptool.entities.Entities.as;
import static com.ksptool.entities.Entities.assign;


@Service
public class AacpModelService {

    @Autowired
    private ModelRepository repository;

    /**
     * 查询模型变体列表
     *
     * @param dto 查询条件
     * @return 查询结果
     */
    public PageResult<GetModelListVo> getModelList(GetModelListDto dto) {
        AacpModelPo query = new AacpModelPo();
        assign(dto, query);

        Page<AacpModelPo> page = repository.getModelList(query, dto.pageRequest());
        if (page.isEmpty()) {
            return PageResult.successWithEmpty();
        }

        List<GetModelListVo> vos = as(page.getContent(), GetModelListVo.class);
        return PageResult.success(vos, (int) page.getTotalElements());
    }

    /**
     * 新增模型变体
     *
     * @param dto 新增条件
     */
    @Transactional(rollbackFor = Exception.class)
    public void addModel(AddModelDto dto) {
        AacpModelPo insertPo = as(dto, AacpModelPo.class);
        repository.save(insertPo);
    }

    /**
     * 编辑模型变体
     *
     * @param dto 编辑条件
     * @throws BizException 业务异常
     */
    @Transactional(rollbackFor = Exception.class)
    public void editModel(EditModelDto dto) throws BizException {
        AacpModelPo updatePo = repository.findById(dto.getId())
                .orElseThrow(() -> new BizException("更新失败,数据不存在或无权限访问."));

        assign(dto, updatePo);
        repository.save(updatePo);
    }

    /**
     * 查询模型变体详情
     *
     * @param dto 查询条件
     * @return 查询结果
     * @throws BizException 业务异常
     */
    public GetModelDetailsVo getModelDetails(CommonIdDto dto) throws BizException {
        AacpModelPo po = repository.findById(dto.getId())
                .orElseThrow(() -> new BizException("查询详情失败,数据不存在或无权限访问."));
        return as(po, GetModelDetailsVo.class);
    }

    /**
     * 删除模型变体
     *
     * @param dto 删除条件
     * @throws BizException 业务异常
     */
    @Transactional(rollbackFor = Exception.class)
    public void removeModel(CommonIdDto dto) throws BizException {
        if (dto.isBatch()) {
            repository.deleteAllById(dto.getIds());
            return;
        }
        repository.deleteById(dto.getId());
    }

}
