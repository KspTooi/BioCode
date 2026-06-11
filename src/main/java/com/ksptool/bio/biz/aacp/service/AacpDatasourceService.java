package com.ksptool.bio.biz.aacp.service;

import com.ksptool.assembly.entity.exception.BizException;
import com.ksptool.assembly.entity.web.CommonIdDto;
import com.ksptool.assembly.entity.web.PageResult;
import com.ksptool.bio.biz.aacp.model.datasource.AacpDatasourcePo;
import com.ksptool.bio.biz.aacp.model.datasource.dto.AddAacpDatasourceDto;
import com.ksptool.bio.biz.aacp.model.datasource.dto.EditAacpDatasourceDto;
import com.ksptool.bio.biz.aacp.model.datasource.dto.GetAacpDatasourceListDto;
import com.ksptool.bio.biz.aacp.model.datasource.vo.GetAacpDatasourceDetailsVo;
import com.ksptool.bio.biz.aacp.model.datasource.vo.GetAacpDatasourceListVo;
import com.ksptool.bio.biz.aacp.repository.AacpDatasourceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.ksptool.entities.Entities.as;
import static com.ksptool.entities.Entities.assign;

@Service
public class AacpDatasourceService {

    @Autowired
    private AacpDatasourceRepository repository;

    /**
     * 查询AACP数据源列表
     *
     * @param dto 查询条件
     * @return 查询结果
     */
    public PageResult<GetAacpDatasourceListVo> getAacpDatasourceList(GetAacpDatasourceListDto dto) {
        AacpDatasourcePo query = new AacpDatasourcePo();
        assign(dto, query);

        Page<AacpDatasourcePo> page = repository.getAacpDatasourceList(query, dto.pageRequest());
        if (page.isEmpty()) {
            return PageResult.successWithEmpty();
        }

        List<GetAacpDatasourceListVo> vos = as(page.getContent(), GetAacpDatasourceListVo.class);
        return PageResult.success(vos, (int) page.getTotalElements());
    }

    /**
     * 新增AACP数据源
     *
     * @param dto 新增条件
     */
    @Transactional(rollbackFor = Exception.class)
    public void addAacpDatasource(AddAacpDatasourceDto dto) throws BizException {
        if (repository.countByCodeExcludeId(dto.getCode(), null) > 0) {
            throw new BizException("唯一编码已存在,请更换后重试.");
        }
        AacpDatasourcePo insertPo = as(dto, AacpDatasourcePo.class);
        repository.save(insertPo);
    }

    /**
     * 编辑AACP数据源
     *
     * @param dto 编辑条件
     * @throws BizException 业务异常
     */
    @Transactional(rollbackFor = Exception.class)
    public void editAacpDatasource(EditAacpDatasourceDto dto) throws BizException {
        if (repository.countByCodeExcludeId(dto.getCode(), dto.getId()) > 0) {
            throw new BizException("唯一编码已存在,请更换后重试.");
        }
        AacpDatasourcePo updatePo = repository.findById(dto.getId())
                .orElseThrow(() -> new BizException("更新失败,数据不存在或无权限访问."));

        assign(dto, updatePo);
        repository.save(updatePo);
    }

    /**
     * 查询AACP数据源详情
     *
     * @param dto 查询条件
     * @return 查询结果
     * @throws BizException 业务异常
     */
    public GetAacpDatasourceDetailsVo getAacpDatasourceDetails(CommonIdDto dto) throws BizException {
        AacpDatasourcePo po = repository.findById(dto.getId())
                .orElseThrow(() -> new BizException("查询详情失败,数据不存在或无权限访问."));
        return as(po, GetAacpDatasourceDetailsVo.class);
    }

    /**
     * 删除AACP数据源
     *
     * @param dto 删除条件
     * @throws BizException 业务异常
     */
    @Transactional(rollbackFor = Exception.class)
    public void removeAacpDatasource(CommonIdDto dto) throws BizException {
        if (dto.isBatch()) {
            repository.deleteAllById(dto.getIds());
            return;
        }
        repository.deleteById(dto.getId());
    }
}
