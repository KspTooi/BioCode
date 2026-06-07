package com.ksptool.bio.biz.aacpfunc.service;

import com.ksptool.assembly.entity.exception.BizException;
import com.ksptool.assembly.entity.web.CommonIdDto;
import com.ksptool.assembly.entity.web.PageResult;
import com.ksptool.bio.biz.aacpfunc.model.AacpFuncPo;
import com.ksptool.bio.biz.aacpfunc.model.dto.AddAacpFuncDto;
import com.ksptool.bio.biz.aacpfunc.model.dto.EditAacpFuncDto;
import com.ksptool.bio.biz.aacpfunc.model.dto.GetAacpFuncListDto;
import com.ksptool.bio.biz.aacpfunc.model.vo.GetAacpFuncDetailsVo;
import com.ksptool.bio.biz.aacpfunc.model.vo.GetAacpFuncListVo;
import com.ksptool.bio.biz.aacpfunc.repository.AacpFuncRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.ksptool.entities.Entities.as;
import static com.ksptool.entities.Entities.assign;

@Service
public class AacpFuncService {

    @Autowired
    private AacpFuncRepository repository;

    /**
     * 查询微函数列表
     *
     * @param dto 查询条件
     * @return 查询结果
     */
    public PageResult<GetAacpFuncListVo> getAacpFuncList(GetAacpFuncListDto dto) {
        AacpFuncPo query = new AacpFuncPo();
        assign(dto, query);

        Page<AacpFuncPo> page = repository.getAacpFuncList(query, dto.pageRequest());
        if (page.isEmpty()) {
            return PageResult.successWithEmpty();
        }

        List<GetAacpFuncListVo> vos = as(page.getContent(), GetAacpFuncListVo.class);
        return PageResult.success(vos, (int) page.getTotalElements());
    }

    /**
     * 新增微函数
     *
     * @param dto 新增条件
     */
    @Transactional(rollbackFor = Exception.class)
    public void addAacpFunc(AddAacpFuncDto dto) {
        AacpFuncPo insertPo = as(dto, AacpFuncPo.class);
        repository.save(insertPo);
    }

    /**
     * 编辑微函数
     *
     * @param dto 编辑条件
     * @throws BizException 业务异常
     */
    @Transactional(rollbackFor = Exception.class)
    public void editAacpFunc(EditAacpFuncDto dto) throws BizException {
        AacpFuncPo updatePo = repository.findById(dto.getId())
                .orElseThrow(() -> new BizException("更新失败,数据不存在或无权限访问."));

        assign(dto, updatePo);
        repository.save(updatePo);
    }

    /**
     * 查询微函数详情
     *
     * @param dto 查询条件
     * @return 查询结果
     * @throws BizException 业务异常
     */
    public GetAacpFuncDetailsVo getAacpFuncDetails(CommonIdDto dto) throws BizException {
        AacpFuncPo po = repository.findById(dto.getId())
                .orElseThrow(() -> new BizException("查询详情失败,数据不存在或无权限访问."));
        return as(po, GetAacpFuncDetailsVo.class);
    }

    /**
     * 删除微函数
     *
     * @param dto 删除条件
     * @throws BizException 业务异常
     */
    @Transactional(rollbackFor = Exception.class)
    public void removeAacpFunc(CommonIdDto dto) throws BizException {
        if (dto.isBatch()) {
            repository.deleteAllById(dto.getIds());
            return;
        }
        repository.deleteById(dto.getId());
    }

}
