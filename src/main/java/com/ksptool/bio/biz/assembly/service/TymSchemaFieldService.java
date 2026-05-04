package com.ksptool.bio.biz.assembly.service;

import com.ksptool.assembly.entity.exception.BizException;
import com.ksptool.assembly.entity.web.CommonIdDto;
import com.ksptool.assembly.entity.web.PageResult;
import com.ksptool.bio.biz.assembly.model.tymschema.TymSchemaPo;
import com.ksptool.bio.biz.assembly.model.tymschemafield.TymSchemaFieldPo;
import com.ksptool.bio.biz.assembly.model.tymschemafield.dto.AddTymSchemaFieldDto;
import com.ksptool.bio.biz.assembly.model.tymschemafield.dto.EditTymSchemaFieldDto;
import com.ksptool.bio.biz.assembly.model.tymschemafield.dto.GetTymSchemaFieldListDto;
import com.ksptool.bio.biz.assembly.model.tymschemafield.vo.GetTymSchemaFieldDetailsVo;
import com.ksptool.bio.biz.assembly.model.tymschemafield.vo.GetTymSchemaFieldListVo;
import com.ksptool.bio.biz.assembly.repository.TymSchemaFieldRepository;
import com.ksptool.bio.biz.assembly.repository.TymSchemaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.ksptool.entities.Entities.as;
import static com.ksptool.entities.Entities.assign;

/**
 * @author KspTooi
 * @since 1.6.19(S).32
 */
@Service
public class TymSchemaFieldService {

    @Autowired
    private TymSchemaFieldRepository repository;

    @Autowired
    private TymSchemaRepository tymSchemaRepository;

    /**
     * 查询类型映射方案字段列表
     *
     * @param dto 查询参数
     * @return 类型映射方案字段列表
     */
    public PageResult<GetTymSchemaFieldListVo> getTymSchemaFieldList(GetTymSchemaFieldListDto dto) {
        TymSchemaFieldPo query = new TymSchemaFieldPo();
        assign(dto, query);

        Page<TymSchemaFieldPo> page = repository.getTymSchemaFieldList(query, dto.pageRequest());
        if (page.isEmpty()) {
            return PageResult.successWithEmpty();
        }

        List<GetTymSchemaFieldListVo> vos = as(page.getContent(), GetTymSchemaFieldListVo.class);
        return PageResult.success(vos, (int) page.getTotalElements());
    }

    /**
     * 新增类型映射方案字段
     *
     * @param dto 新增参数
     */
    @Transactional(rollbackFor = Exception.class)
    public void addTymSchemaField(AddTymSchemaFieldDto dto) throws BizException {

        //查询类型映射方案是否存在
        TymSchemaPo tymSchemaPo = tymSchemaRepository.findById(dto.getTypeSchemaId())
                .orElseThrow(() -> new BizException("类型映射方案不存在"));

        TymSchemaFieldPo insertPo = as(dto, TymSchemaFieldPo.class);
        insertPo.setTypeSchemaId(tymSchemaPo.getId());
        repository.save(insertPo);

        //更新类型映射方案字段数量
        tymSchemaPo.setTypeCount(tymSchemaPo.getTypeCount() + 1);
        tymSchemaRepository.save(tymSchemaPo);
    }

    /**
     * 编辑类型映射方案字段
     *
     * @param dto 编辑参数
     * @throws BizException
     */
    @Transactional(rollbackFor = Exception.class)
    public void editTymSchemaField(EditTymSchemaFieldDto dto) throws BizException {
        TymSchemaFieldPo updatePo = repository.findById(dto.getId())
                .orElseThrow(() -> new BizException("更新失败,数据不存在或无权限访问."));

        assign(dto, updatePo);
        repository.save(updatePo);
    }

    /**
     * 查询类型映射方案字段详情
     *
     * @param dto 查询参数
     * @return 类型映射方案字段详情
     * @throws BizException
     */
    public GetTymSchemaFieldDetailsVo getTymSchemaFieldDetails(CommonIdDto dto) throws BizException {
        TymSchemaFieldPo po = repository.findById(dto.getId())
                .orElseThrow(() -> new BizException("查询详情失败,数据不存在或无权限访问."));
        return as(po, GetTymSchemaFieldDetailsVo.class);
    }

    /**
     * 删除类型映射方案字段
     *
     * @param dto 删除参数
     * @throws BizException
     */
    @Transactional(rollbackFor = Exception.class)
    public void removeTymSchemaField(CommonIdDto dto) throws BizException {
        //不支持批量删除
        if (dto.isBatch()) {
            throw new BizException("不支持批量删除");
        }

        //查询TYMSF 
        TymSchemaFieldPo po = repository.findById(dto.getId())
                .orElseThrow(() -> new BizException("删除失败,数据不存在或无权限访问."));

        //查询类型映射方案是否存在
        TymSchemaPo tymSchemaPo = tymSchemaRepository.findById(po.getTypeSchemaId()).orElse(null);

        //先删除
        repository.deleteById(dto.getId());

        //更新类型映射方案字段数量
        if (tymSchemaPo != null) {
            tymSchemaPo.setTypeCount(tymSchemaPo.getTypeCount() - 1);

            if (tymSchemaPo.getTypeCount() < 1) {
                tymSchemaPo.setTypeCount(0);
            }

            tymSchemaRepository.save(tymSchemaPo);
        }

    }

}