package com.ksptool.bio.biz.aacp.service;

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
import com.ksptool.bio.biz.aacp.repository.ProviderRepository;
import com.ksptool.bio.biz.aacp.model.provider.ProviderPo;
import com.ksptool.bio.biz.aacp.model.provider.vo.GetProviderListVo;
import com.ksptool.bio.biz.aacp.model.provider.dto.GetProviderListDto;
import com.ksptool.bio.biz.aacp.model.provider.vo.GetProviderDetailsVo;
import com.ksptool.bio.biz.aacp.model.provider.dto.EditProviderDto;
import com.ksptool.bio.biz.aacp.model.provider.dto.AddProviderDto;


@Service
public class ProviderService {

    @Autowired
    private ProviderRepository repository;

    /**
     * 查询模型供应商列表
     * @param dto 查询条件
     * @return 查询结果
     */
    public PageResult<GetProviderListVo> getProviderList(GetProviderListDto dto){
        ProviderPo query = new ProviderPo();
        assign(dto,query);

        Page<ProviderPo> page = repository.getProviderList(query, dto.pageRequest());
        if (page.isEmpty()) {
            return PageResult.successWithEmpty();
        }

        List<GetProviderListVo> vos = as(page.getContent(), GetProviderListVo.class);
        return PageResult.success(vos, (int) page.getTotalElements());
    }

    /**
     * 新增模型供应商
     * @param dto 新增条件
     */
    @Transactional(rollbackFor = Exception.class)
    public void addProvider(AddProviderDto dto){
        ProviderPo insertPo = as(dto,ProviderPo.class);
        repository.save(insertPo);
    }

    /**
     * 编辑模型供应商
     * @param dto 编辑条件
     * @throws BizException 业务异常
     */
    @Transactional(rollbackFor = Exception.class)
    public void editProvider(EditProviderDto dto) throws BizException {
        ProviderPo updatePo = repository.findById(dto.getId())
            .orElseThrow(()-> new BizException("更新失败,数据不存在或无权限访问."));

        assign(dto,updatePo);
        repository.save(updatePo);
    }

    /**
     * 查询模型供应商详情
     * @param dto 查询条件
     * @return 查询结果
     * @throws BizException 业务异常
     */
    public GetProviderDetailsVo getProviderDetails(CommonIdDto dto) throws BizException {
        ProviderPo po = repository.findById(dto.getId())
            .orElseThrow(()-> new BizException("查询详情失败,数据不存在或无权限访问."));
        return as(po,GetProviderDetailsVo.class);
    }

    /**
     * 删除模型供应商
     * @param dto 删除条件
     * @throws BizException 业务异常
     */
    @Transactional(rollbackFor = Exception.class)
    public void removeProvider(CommonIdDto dto) throws BizException {
        if (dto.isBatch()) {
            repository.deleteAllById(dto.getIds());
            return;
        }
        repository.deleteById(dto.getId());
    }

}
