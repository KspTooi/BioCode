package com.ksptool.bio.biz.aacpcapability.service;

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
import com.ksptool.bio.biz.aacpcapability.repository.AacpCapabilityRepository;
import com.ksptool.bio.biz.aacpcapability.model.AacpCapabilityPo;
import com.ksptool.bio.biz.aacpcapability.model.vo.GetAacpCapabilityListVo;
import com.ksptool.bio.biz.aacpcapability.model.dto.GetAacpCapabilityListDto;
import com.ksptool.bio.biz.aacpcapability.model.vo.GetAacpCapabilityDetailsVo;
import com.ksptool.bio.biz.aacpcapability.model.dto.EditAacpCapabilityDto;
import com.ksptool.bio.biz.aacpcapability.model.dto.AddAacpCapabilityDto;


@Service
public class AacpCapabilityService {

    @Autowired
    private AacpCapabilityRepository repository;

    /**
     * 查询能力包列表
     * @param dto 查询条件
     * @return 查询结果
     */
    public PageResult<GetAacpCapabilityListVo> getAacpCapabilityList(GetAacpCapabilityListDto dto){
        AacpCapabilityPo query = new AacpCapabilityPo();
        assign(dto,query);

        Page<AacpCapabilityPo> page = repository.getAacpCapabilityList(query, dto.pageRequest());
        if (page.isEmpty()) {
            return PageResult.successWithEmpty();
        }

        List<GetAacpCapabilityListVo> vos = as(page.getContent(), GetAacpCapabilityListVo.class);
        return PageResult.success(vos, (int) page.getTotalElements());
    }

    /**
     * 新增能力包
     * @param dto 新增条件
     */
    @Transactional(rollbackFor = Exception.class)
    public void addAacpCapability(AddAacpCapabilityDto dto){
        AacpCapabilityPo insertPo = as(dto,AacpCapabilityPo.class);
        repository.save(insertPo);
    }

    /**
     * 编辑能力包
     * @param dto 编辑条件
     * @throws BizException 业务异常
     */
    @Transactional(rollbackFor = Exception.class)
    public void editAacpCapability(EditAacpCapabilityDto dto) throws BizException {
        AacpCapabilityPo updatePo = repository.findById(dto.getId())
            .orElseThrow(()-> new BizException("更新失败,数据不存在或无权限访问."));

        assign(dto,updatePo);
        repository.save(updatePo);
    }

    /**
     * 查询能力包详情
     * @param dto 查询条件
     * @return 查询结果
     * @throws BizException 业务异常
     */
    public GetAacpCapabilityDetailsVo getAacpCapabilityDetails(CommonIdDto dto) throws BizException {
        AacpCapabilityPo po = repository.findById(dto.getId())
            .orElseThrow(()-> new BizException("查询详情失败,数据不存在或无权限访问."));
        return as(po,GetAacpCapabilityDetailsVo.class);
    }

    /**
     * 删除能力包
     * @param dto 删除条件
     * @throws BizException 业务异常
     */
    @Transactional(rollbackFor = Exception.class)
    public void removeAacpCapability(CommonIdDto dto) throws BizException {
        if (dto.isBatch()) {
            repository.deleteAllById(dto.getIds());
            return;
        }
        repository.deleteById(dto.getId());
    }

}
