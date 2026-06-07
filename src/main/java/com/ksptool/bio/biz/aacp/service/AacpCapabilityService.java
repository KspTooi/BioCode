package com.ksptool.bio.biz.aacp.service;

import com.ksptool.assembly.entity.exception.BizException;
import com.ksptool.assembly.entity.web.CommonIdDto;
import com.ksptool.assembly.entity.web.PageResult;
import com.ksptool.bio.biz.aacp.model.AacpCapabilityPo;
import com.ksptool.bio.biz.aacp.model.AacpMcpCapabilityPo;
import com.ksptool.bio.biz.aacp.model.dto.AddAacpCapabilityDto;
import com.ksptool.bio.biz.aacp.model.dto.EditAacpCapabilityDto;
import com.ksptool.bio.biz.aacp.model.dto.GetAacpCapabilityListDto;
import com.ksptool.bio.biz.aacp.model.vo.GetAacpCapabilityDetailsVo;
import com.ksptool.bio.biz.aacp.model.vo.GetAacpCapabilityListVo;
import com.ksptool.bio.biz.aacp.repository.AacpCapabilityRepository;
import com.ksptool.bio.biz.aacp.repository.AacpMcpCapabilityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.ksptool.entities.Entities.as;
import static com.ksptool.entities.Entities.assign;

@Service
public class AacpCapabilityService {

    @Autowired
    private AacpCapabilityRepository repository;

    @Autowired
    private AacpMcpCapabilityRepository mcpCapabilityRepository;

    /**
     * 查询能力包列表
     *
     * @param dto 查询条件
     * @return 查询结果
     */
    public PageResult<GetAacpCapabilityListVo> getAacpCapabilityList(GetAacpCapabilityListDto dto) {
        AacpCapabilityPo query = new AacpCapabilityPo();
        assign(dto, query);

        Page<AacpCapabilityPo> page = repository.getAacpCapabilityList(query, dto.pageRequest());
        if (page.isEmpty()) {
            return PageResult.successWithEmpty();
        }

        List<GetAacpCapabilityListVo> vos = as(page.getContent(), GetAacpCapabilityListVo.class);
        return PageResult.success(vos, (int) page.getTotalElements());
    }

    /**
     * 新增能力包
     *
     * @param dto 新增条件
     */
    @Transactional(rollbackFor = Exception.class)
    public void addAacpCapability(AddAacpCapabilityDto dto) {
        AacpCapabilityPo insertPo = as(dto, AacpCapabilityPo.class);
        repository.save(insertPo);
    }

    /**
     * 编辑能力包
     *
     * @param dto 编辑条件
     * @throws BizException 业务异常
     */
    @Transactional(rollbackFor = Exception.class)
    public void editAacpCapability(EditAacpCapabilityDto dto) throws BizException {
        AacpCapabilityPo updatePo = repository.findById(dto.getId())
                .orElseThrow(() -> new BizException("更新失败,数据不存在或无权限访问."));

        assign(dto, updatePo);
        repository.save(updatePo);
    }

    /**
     * 查询能力包详情
     *
     * @param dto 查询条件
     * @return 查询结果
     * @throws BizException 业务异常
     */
    public GetAacpCapabilityDetailsVo getAacpCapabilityDetails(CommonIdDto dto) throws BizException {
        AacpCapabilityPo po = repository.findById(dto.getId())
                .orElseThrow(() -> new BizException("查询详情失败,数据不存在或无权限访问."));
        return as(po, GetAacpCapabilityDetailsVo.class);
    }

    /**
     * 删除能力包
     *
     * @param dto 删除条件
     * @throws BizException 业务异常
     */
    @Transactional(rollbackFor = Exception.class)
    public void removeAacpCapability(CommonIdDto dto) throws BizException {
        if (dto.isBatch()) {
            throw new BizException("能力包不支持批量删除");
        }
        long refCount = mcpCapabilityRepository.countByCapabilityId(dto.getId());
        if (refCount > 0) {
            throw new BizException("该能力包已被" + refCount + "台MCP服务器使用，无法删除");
        }
        repository.deleteById(dto.getId());
    }

}
