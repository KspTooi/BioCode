package com.ksptool.bio.biz.assembly.service;

import com.ksptool.assembly.entity.exception.BizException;
import com.ksptool.assembly.entity.web.CommonIdDto;
import com.ksptool.assembly.entity.web.PageResult;
import com.ksptool.bio.biz.assembly.model.polytemplatefield.PolyTemplateFieldPo;
import com.ksptool.bio.biz.assembly.model.polytemplatefield.dto.AddPolyTemplateFieldDto;
import com.ksptool.bio.biz.assembly.model.polytemplatefield.dto.EditPolyTemplateFieldDto;
import com.ksptool.bio.biz.assembly.model.polytemplatefield.dto.GetPolyTemplateFieldListDto;
import com.ksptool.bio.biz.assembly.model.polytemplatefield.vo.GetPolyTemplateFieldDetailsVo;
import com.ksptool.bio.biz.assembly.model.polytemplatefield.vo.GetPolyTemplateFieldListVo;
import com.ksptool.bio.biz.assembly.repository.PolyTemplateFieldRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.ksptool.entities.Entities.as;
import static com.ksptool.entities.Entities.assign;


@Service
public class PolyTemplateFieldService {

    @Autowired
    private PolyTemplateFieldRepository repository;

    /**
     * 查询聚合模板字段列表
     *
     * @param dto 查询条件
     * @return 查询结果
     */
    public PageResult<GetPolyTemplateFieldListVo> getPolyTemplateFieldList(GetPolyTemplateFieldListDto dto) {
        PolyTemplateFieldPo query = new PolyTemplateFieldPo();
        assign(dto, query);

        Page<PolyTemplateFieldPo> page = repository.getPolyTemplateFieldList(query, dto.pageRequest());
        if (page.isEmpty()) {
            return PageResult.successWithEmpty();
        }

        List<GetPolyTemplateFieldListVo> vos = as(page.getContent(), GetPolyTemplateFieldListVo.class);
        return PageResult.success(vos, (int) page.getTotalElements());
    }

    /**
     * 新增聚合模板字段
     *
     * @param dto 新增条件
     */
    @Transactional(rollbackFor = Exception.class)
    public void addPolyTemplateField(AddPolyTemplateFieldDto dto) {
        PolyTemplateFieldPo insertPo = as(dto, PolyTemplateFieldPo.class);
        repository.save(insertPo);
    }

    /**
     * 编辑聚合模板字段
     *
     * @param dto 编辑条件
     * @throws BizException 业务异常
     */
    @Transactional(rollbackFor = Exception.class)
    public void editPolyTemplateField(EditPolyTemplateFieldDto dto) throws BizException {
        PolyTemplateFieldPo updatePo = repository.findById(dto.getId())
                .orElseThrow(() -> new BizException("更新失败,数据不存在或无权限访问."));

        assign(dto, updatePo);
        repository.save(updatePo);
    }

    /**
     * 查询聚合模板字段详情
     *
     * @param dto 查询条件
     * @return 查询结果
     * @throws BizException 业务异常
     */
    public GetPolyTemplateFieldDetailsVo getPolyTemplateFieldDetails(CommonIdDto dto) throws BizException {
        PolyTemplateFieldPo po = repository.findById(dto.getId())
                .orElseThrow(() -> new BizException("查询详情失败,数据不存在或无权限访问."));
        return as(po, GetPolyTemplateFieldDetailsVo.class);
    }

    /**
     * 删除聚合模板字段
     *
     * @param dto 删除条件
     * @throws BizException 业务异常
     */
    @Transactional(rollbackFor = Exception.class)
    public void removePolyTemplateField(CommonIdDto dto) throws BizException {
        if (dto.isBatch()) {
            repository.deleteAllById(dto.getIds());
            return;
        }
        repository.deleteById(dto.getId());
    }

}
