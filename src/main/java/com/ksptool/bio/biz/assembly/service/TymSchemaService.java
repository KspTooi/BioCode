package com.ksptool.bio.biz.assembly.service;

import com.ksptool.assembly.entity.exception.BizException;
import com.ksptool.assembly.entity.web.CommonIdDto;
import com.ksptool.assembly.entity.web.PageResult;
import com.ksptool.bio.biz.assembly.model.tymschema.TymSchemaPo;
import com.ksptool.bio.biz.assembly.model.tymschema.dto.AddTymSchemaDto;
import com.ksptool.bio.biz.assembly.model.tymschema.dto.EditTymSchemaDto;
import com.ksptool.bio.biz.assembly.model.tymschema.dto.GetTymSchemaListDto;
import com.ksptool.bio.biz.assembly.model.tymschema.vo.GetTymSchemaDetailsVo;
import com.ksptool.bio.biz.assembly.model.tymschema.vo.GetTymSchemaListVo;
import com.ksptool.bio.biz.assembly.repository.OpSchemaRepository;
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
public class TymSchemaService {

    @Autowired
    private TymSchemaRepository repository;

    @Autowired
    private OpSchemaRepository opSchemaRepository;

    /**
     * 查询类型映射方案列表
     *
     * @param dto 查询参数
     * @return 类型映射方案列表
     */
    public PageResult<GetTymSchemaListVo> getTymSchemaList(GetTymSchemaListDto dto) {
        TymSchemaPo query = new TymSchemaPo();
        assign(dto, query);

        Page<TymSchemaPo> page = repository.getTymSchemaList(query, dto.pageRequest());
        if (page.isEmpty()) {
            return PageResult.successWithEmpty();
        }

        List<GetTymSchemaListVo> vos = as(page.getContent(), GetTymSchemaListVo.class);
        return PageResult.success(vos, (int) page.getTotalElements());
    }

    /**
     * 新增类型映射方案
     *
     * @param dto 新增参数
     */
    @Transactional(rollbackFor = Exception.class)
    public void addTymSchema(AddTymSchemaDto dto) throws BizException {

        //校验编码是否唯一
        if (repository.countByCode(dto.getCode()) > 0) {
            throw new BizException("编码已存在:[" + dto.getCode() + "]");
        }

        TymSchemaPo insertPo = as(dto, TymSchemaPo.class);
        insertPo.setTypeCount(0);
        repository.save(insertPo);
    }

    /**
     * 编辑类型映射方案
     *
     * @param dto 编辑参数
     * @throws BizException
     */
    @Transactional(rollbackFor = Exception.class)
    public void editTymSchema(EditTymSchemaDto dto) throws BizException {
        TymSchemaPo updatePo = repository.findById(dto.getId())
                .orElseThrow(() -> new BizException("更新失败,数据不存在或无权限访问."));

        //校验编码是否唯一
        if (repository.countByCodeExcludeId(dto.getCode(), updatePo.getId()) > 0) {
            throw new BizException("编码已存在:[" + dto.getCode() + "]");
        }

        assign(dto, updatePo);
        repository.save(updatePo);
    }

    /**
     * 查询类型映射方案详情
     *
     * @param dto 查询参数
     * @return 类型映射方案详情
     * @throws BizException
     */
    public GetTymSchemaDetailsVo getTymSchemaDetails(CommonIdDto dto) throws BizException {
        TymSchemaPo po = repository.findById(dto.getId())
                .orElseThrow(() -> new BizException("查询详情失败,数据不存在或无权限访问."));
        return as(po, GetTymSchemaDetailsVo.class);
    }

    /**
     * 删除类型映射方案
     *
     * @param dto 删除参数
     * @throws BizException
     */
    @Transactional(rollbackFor = Exception.class)
    public void removeTymSchema(CommonIdDto dto) throws BizException {

        //删除时检查是否被输出方案使用
        if (opSchemaRepository.countByTypeSchemaId(dto.getId()) > 0) {
            throw new BizException("该类型映射方案已被输出方案使用,无法执行删除操作!");
        }

        repository.deleteById(dto.getId());
    }

}