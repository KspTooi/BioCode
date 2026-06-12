package com.ksptool.bio.biz.auth.service;

import com.ksptool.assembly.entity.exception.BizException;
import com.ksptool.assembly.entity.web.CommonIdDto;
import com.ksptool.assembly.entity.web.PageResult;
import com.ksptool.bio.biz.auth.model.basicpat.BasicPatPo;
import com.ksptool.bio.biz.auth.model.basicpat.dto.AddBasicPatDto;
import com.ksptool.bio.biz.auth.model.basicpat.dto.EditBasicPatDto;
import com.ksptool.bio.biz.auth.model.basicpat.dto.GetBasicPatListDto;
import com.ksptool.bio.biz.auth.model.basicpat.vo.GetBasicPatDetailsVo;
import com.ksptool.bio.biz.auth.model.basicpat.vo.GetBasicPatListVo;
import com.ksptool.bio.biz.auth.repository.BasicPatRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.ksptool.entities.Entities.as;
import static com.ksptool.entities.Entities.assign;

/**
 * 基本PAT业务逻辑
 */
@Service
public class BasicPatService {

    @Autowired
    private BasicPatRepository repository;

    /**
     * 查询基本PAT列表
     *
     * @param dto 查询条件
     * @return 查询结果
     */
    public PageResult<GetBasicPatListVo> getBasicPatList(GetBasicPatListDto dto) {
        BasicPatPo query = new BasicPatPo();
        assign(dto, query);

        Page<BasicPatPo> page = repository.getBasicPatList(query, dto.pageRequest());
        if (page.isEmpty()) {
            return PageResult.successWithEmpty();
        }

        List<GetBasicPatListVo> vos = as(page.getContent(), GetBasicPatListVo.class);
        return PageResult.success(vos, (int) page.getTotalElements());
    }

    /**
     * 新增基本PAT
     *
     * @param dto 新增条件
     */
    @Transactional(rollbackFor = Exception.class)
    public void addBasicPat(AddBasicPatDto dto) {
        BasicPatPo insertPo = as(dto, BasicPatPo.class);
        repository.save(insertPo);
    }

    /**
     * 编辑基本PAT
     *
     * @param dto 编辑条件
     * @throws BizException 业务异常
     */
    @Transactional(rollbackFor = Exception.class)
    public void editBasicPat(EditBasicPatDto dto) throws BizException {
        BasicPatPo updatePo = repository.findById(dto.getId())
                .orElseThrow(() -> new BizException("更新失败,数据不存在或无权限访问."));

        assign(dto, updatePo);
        repository.save(updatePo);
    }

    /**
     * 查询基本PAT详情
     *
     * @param dto 查询条件
     * @return 查询结果
     * @throws BizException 业务异常
     */
    public GetBasicPatDetailsVo getBasicPatDetails(CommonIdDto dto) throws BizException {
        BasicPatPo po = repository.findById(dto.getId())
                .orElseThrow(() -> new BizException("查询详情失败,数据不存在或无权限访问."));
        return as(po, GetBasicPatDetailsVo.class);
    }

    /**
     * 删除基本PAT
     *
     * @param dto 删除条件
     * @throws BizException 业务异常
     */
    @Transactional(rollbackFor = Exception.class)
    public void removeBasicPat(CommonIdDto dto) throws BizException {
        if (dto.isBatch()) {
            repository.deleteAllById(dto.getIds());
            return;
        }
        repository.deleteById(dto.getId());
    }
}
