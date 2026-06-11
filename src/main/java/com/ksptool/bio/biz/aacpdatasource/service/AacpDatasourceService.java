package com.ksptool.bio.biz.aacpdatasource.service;

import com.ksptool.assembly.entity.web.PageResult;
import com.ksptool.assembly.entity.web.CommonIdDto;
import com.ksptool.assembly.entity.exception.BizException;
import static com.ksptool.entities.Entities.as;
import static com.ksptool.entities.Entities.assign;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Page;
import java.util.Optional;
import com.ksptool.bio.biz.aacpdatasource.repository.AacpDatasourceRepository;
import com.ksptool.bio.biz.aacpdatasource.model.AacpDatasourcePo;
import com.ksptool.bio.biz.aacpdatasource.model.vo.GetAacpDatasourceListVo;
import com.ksptool.bio.biz.aacpdatasource.model.dto.GetAacpDatasourceListDto;
import com.ksptool.bio.biz.aacpdatasource.model.vo.GetAacpDatasourceDetailsVo;
import com.ksptool.bio.biz.aacpdatasource.model.dto.EditAacpDatasourceDto;
import com.ksptool.bio.biz.aacpdatasource.model.dto.AddAacpDatasourceDto;


@Service
public class AacpDatasourceService {

    @Autowired
    private AacpDatasourceRepository repository;

    /**
     * 查询AACP数据源列表
     * @param dto 查询条件
     * @return 查询结果
     */
    public PageResult<GetAacpDatasourceListVo> getAacpDatasourceList(GetAacpDatasourceListDto dto){
        AacpDatasourcePo query = new AacpDatasourcePo();
        assign(dto,query);

        Page<AacpDatasourcePo> page = repository.getAacpDatasourceList(query, dto.pageRequest());
        if (page.isEmpty()) {
            return PageResult.successWithEmpty();
        }

        List<GetAacpDatasourceListVo> vos = as(page.getContent(), GetAacpDatasourceListVo.class);
        return PageResult.success(vos, (int) page.getTotalElements());
    }

    /**
     * 新增AACP数据源
     * @param dto 新增条件
     */
    @Transactional(rollbackFor = Exception.class)
    public void addAacpDatasource(AddAacpDatasourceDto dto){
        AacpDatasourcePo insertPo = as(dto,AacpDatasourcePo.class);
        repository.save(insertPo);
    }

    /**
     * 编辑AACP数据源
     * @param dto 编辑条件
     * @throws BizException 业务异常
     */
    @Transactional(rollbackFor = Exception.class)
    public void editAacpDatasource(EditAacpDatasourceDto dto) throws BizException {
        AacpDatasourcePo updatePo = repository.findById(dto.getId())
            .orElseThrow(()-> new BizException("更新失败,数据不存在或无权限访问."));

        assign(dto,updatePo);
        repository.save(updatePo);
    }

    /**
     * 查询AACP数据源详情
     * @param dto 查询条件
     * @return 查询结果
     * @throws BizException 业务异常
     */
    public GetAacpDatasourceDetailsVo getAacpDatasourceDetails(CommonIdDto dto) throws BizException {
        AacpDatasourcePo po = repository.findById(dto.getId())
            .orElseThrow(()-> new BizException("查询详情失败,数据不存在或无权限访问."));
        return as(po,GetAacpDatasourceDetailsVo.class);
    }

    /**
     * 删除AACP数据源
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
