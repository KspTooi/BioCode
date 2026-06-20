package com.ksptool.bio.biz.assembly.service;

import com.ksptool.assembly.entity.exception.BizException;
import com.ksptool.assembly.entity.web.CommonIdDto;
import com.ksptool.assembly.entity.web.PageResult;
import com.ksptool.bio.biz.assembly.model.polytemplate.PolyTemplatePo;
import com.ksptool.bio.biz.assembly.model.polytemplate.dto.AddPolyTemplateDto;
import com.ksptool.bio.biz.assembly.model.polytemplate.dto.EditPolyTemplateDto;
import com.ksptool.bio.biz.assembly.model.polytemplate.dto.GetPolyTemplateListDto;
import com.ksptool.bio.biz.assembly.model.polytemplate.vo.GetPolyTemplateDetailsVo;
import com.ksptool.bio.biz.assembly.model.polytemplate.vo.GetPolyTemplateListVo;
import com.ksptool.bio.biz.assembly.repository.PolyTemplateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.ksptool.entities.Entities.as;
import static com.ksptool.entities.Entities.assign;

/**
 * @author KspTooi
 * @since 1.7.9(I).1
 */
@Service
public class PolyTemplateService {

    @Autowired
    private PolyTemplateRepository repository;

    /**
     * 查询聚合模板列表
     *
     * @param dto 查询条件
     * @return 查询结果
     */
    public PageResult<GetPolyTemplateListVo> getPolyTemplateList(GetPolyTemplateListDto dto) {
        PolyTemplatePo query = new PolyTemplatePo();
        assign(dto, query);

        Page<PolyTemplatePo> page = repository.getPolyTemplateList(query, dto.pageRequest());
        if (page.isEmpty()) {
            return PageResult.successWithEmpty();
        }

        List<GetPolyTemplateListVo> vos = as(page.getContent(), GetPolyTemplateListVo.class);
        return PageResult.success(vos, (int) page.getTotalElements());
    }

    /**
     * 新增聚合模板
     *
     * @param dto 新增条件
     */
    @Transactional(rollbackFor = Exception.class)
    public void addPolyTemplate(AddPolyTemplateDto dto) {
        PolyTemplatePo insertPo = as(dto, PolyTemplatePo.class);
        repository.save(insertPo);
    }

    /**
     * 编辑聚合模板
     *
     * @param dto 编辑条件
     * @throws BizException 业务异常
     */
    @Transactional(rollbackFor = Exception.class)
    public void editPolyTemplate(EditPolyTemplateDto dto) throws BizException {
        PolyTemplatePo updatePo = repository.findById(dto.getId())
                .orElseThrow(() -> new BizException("更新失败,数据不存在或无权限访问."));

        assign(dto, updatePo);
        repository.save(updatePo);
    }

    /**
     * 查询聚合模板详情
     *
     * @param dto 查询条件
     * @return 查询结果
     * @throws BizException 业务异常
     */
    public GetPolyTemplateDetailsVo getPolyTemplateDetails(CommonIdDto dto) throws BizException {
        PolyTemplatePo po = repository.findById(dto.getId())
                .orElseThrow(() -> new BizException("查询详情失败,数据不存在或无权限访问."));
        return as(po, GetPolyTemplateDetailsVo.class);
    }

    /**
     * 删除聚合模板
     *
     * @param dto 删除条件
     * @throws BizException 业务异常
     */
    @Transactional(rollbackFor = Exception.class)
    public void removePolyTemplate(CommonIdDto dto) throws BizException {
        if (dto.isBatch()) {
            repository.deleteAllById(dto.getIds());
            return;
        }
        repository.deleteById(dto.getId());
    }

}
