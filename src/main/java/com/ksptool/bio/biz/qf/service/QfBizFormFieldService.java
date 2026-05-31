package com.ksptool.bio.biz.qf.service;

import com.ksptool.assembly.entity.web.PageResult;
import com.ksptool.assembly.entity.web.CommonIdDto;
import com.ksptool.assembly.entity.exception.BizException;
import static com.ksptool.entities.Entities.as;
import static com.ksptool.entities.Entities.assign;

import com.ksptool.bio.biz.qf.model.qfbizform.QfBizFormPo;
import com.ksptool.bio.biz.qf.model.qfbizformfield.QfBizFormFieldPo;
import com.ksptool.bio.biz.qf.model.qfbizformfield.dto.AddQfBizFormFieldDto;
import com.ksptool.bio.biz.qf.model.qfbizformfield.dto.EditQfBizFormFieldDto;
import com.ksptool.bio.biz.qf.model.qfbizformfield.dto.GetQfBizFormFieldListDto;
import com.ksptool.bio.biz.qf.model.qfbizformfield.vo.GetQfBizFormFieldDetailsVo;
import com.ksptool.bio.biz.qf.model.qfbizformfield.vo.GetQfBizFormFieldListVo;
import com.ksptool.bio.biz.qf.repository.QfBizFormFieldRepository;
import com.ksptool.bio.biz.qf.repository.QfBizFormRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;


@Service
public class QfBizFormFieldService {

    @Autowired
    private QfBizFormFieldRepository repository;

    @Autowired
    private QfBizFormRepository qfBizFormRepository;

    /**
     * 查询流程表单字段配置列表
     * @param dto 查询条件
     * @return 查询结果
     */
    public PageResult<GetQfBizFormFieldListVo> getQfBizFormFieldList(GetQfBizFormFieldListDto dto){


        Page<QfBizFormFieldPo> page = repository.getQfBizFormFieldList(dto, dto.pageRequest());
        if (page.isEmpty()) {
            return PageResult.successWithEmpty();
        }

        List<GetQfBizFormFieldListVo> vos = as(page.getContent(), GetQfBizFormFieldListVo.class);
        return PageResult.success(vos, (int) page.getTotalElements());
    }

    /**
     * 新增流程表单字段配置
     * @param dto 新增条件
     */
    @Transactional(rollbackFor = Exception.class)
    public void addQfBizFormField(AddQfBizFormFieldDto dto)throws BizException{
        QfBizFormPo formPo = qfBizFormRepository.findById(dto.getFormId())
            .orElseThrow(() -> new BizException("表单不存在"));
        
        if (repository.existsByFormIdAndFieldName(dto.getFormId(), dto.getFieldName())) {
            throw new BizException("该表单下字段名已存在");
        }
        
        QfBizFormFieldPo insertPo = as(dto,QfBizFormFieldPo.class);
        repository.save(insertPo);
    }

    /**
     * 编辑流程表单字段配置
     * @param dto 编辑条件
     * @throws BizException 业务异常
     */
    @Transactional(rollbackFor = Exception.class)
    public void editQfBizFormField(EditQfBizFormFieldDto dto) throws BizException {
        QfBizFormFieldPo updatePo = repository.findById(dto.getId())
            .orElseThrow(()-> new BizException("更新失败,数据不存在或无权限访问."));

        if (dto.getFormId() != null) {
            QfBizFormPo formPo = qfBizFormRepository.findById(dto.getFormId())
                .orElseThrow(() -> new BizException("表单不存在"));
        }

        Long formIdToCheck = dto.getFormId() != null ? dto.getFormId() : updatePo.getFormId();
        if (repository.existsByFormIdAndFieldNameAndIdNot(formIdToCheck, dto.getFieldName(), dto.getId())) {
            throw new BizException("该表单下字段名已存在");
        }

        assign(dto,updatePo);
        repository.save(updatePo);
    }

    /**
     * 查询流程表单字段配置详情
     * @param dto 查询条件
     * @return 查询结果
     * @throws BizException 业务异常
     */
    public GetQfBizFormFieldDetailsVo getQfBizFormFieldDetails(CommonIdDto dto) throws BizException {
        QfBizFormFieldPo po = repository.findById(dto.getId())
            .orElseThrow(()-> new BizException("查询详情失败,数据不存在或无权限访问."));
        return as(po,GetQfBizFormFieldDetailsVo.class);
    }

    /**
     * 删除流程表单字段配置
     * @param dto 删除条件
     * @throws BizException 业务异常
     */
    @Transactional(rollbackFor = Exception.class)
    public void removeQfBizFormField(CommonIdDto dto) throws BizException {
        if (dto.isBatch()) {
            repository.deleteAllById(dto.getIds());
            return;
        }
        repository.deleteById(dto.getId());
    }

}
