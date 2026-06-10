package com.ksptool.bio.biz.aacp.service;

import com.ksptool.assembly.entity.exception.BizException;
import com.ksptool.assembly.entity.web.CommonIdDto;
import com.ksptool.assembly.entity.web.PageResult;
import com.ksptool.bio.biz.aacp.model.AacpCapabilityFuncPo;
import com.ksptool.bio.biz.aacp.model.capability.AacpCapabilityPo;
import com.ksptool.bio.biz.aacp.model.capability.dto.AddAacpCapabilityDto;
import com.ksptool.bio.biz.aacp.model.capability.dto.EditAacpCapabilityDto;
import com.ksptool.bio.biz.aacp.model.capability.dto.GetAacpCapabilityListDto;
import com.ksptool.bio.biz.aacp.model.capability.vo.GetAacpCapabilityDetailsVo;
import com.ksptool.bio.biz.aacp.model.capability.vo.GetAacpCapabilityListVo;
import com.ksptool.bio.biz.aacp.repository.AacpCapabilityFuncRepository;
import com.ksptool.bio.biz.aacp.repository.AacpCapabilityRepository;
import com.ksptool.bio.biz.aacp.repository.AacpMcpCapabilityRepository;
import com.ksptool.bio.biz.core.common.IdsDiff;
import jakarta.persistence.Tuple;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.ksptool.bio.biz.core.common.TupleMapper.tupleAs;
import static com.ksptool.entities.Entities.as;
import static com.ksptool.entities.Entities.assign;

@Service
public class AacpCapabilityService {

    @Autowired
    private AacpCapabilityRepository repository;

    @Autowired
    private AacpMcpCapabilityRepository mcpCapabilityRepository;

    @Autowired
    private AacpCapabilityFuncRepository capabilityFuncRepository;

    /**
     * 查询能力包列表
     *
     * @param dto 查询条件
     * @return 查询结果
     */
    public PageResult<GetAacpCapabilityListVo> getAacpCapabilityList(GetAacpCapabilityListDto dto) {
        AacpCapabilityPo query = new AacpCapabilityPo();
        assign(dto, query);

        Page<Tuple> page = repository.getAacpCapabilityList(query, dto.pageRequest());
        if (page.isEmpty()) {
            return PageResult.successWithEmpty();
        }

        List<GetAacpCapabilityListVo> vos = tupleAs(page.getContent(), GetAacpCapabilityListVo.class);
        return PageResult.success(vos, (int) page.getTotalElements());
    }

    /**
     * 新增能力包
     *
     * @param dto 新增条件
     * @throws BizException 业务异常
     */
    @Transactional(rollbackFor = Exception.class)
    public void addAacpCapability(AddAacpCapabilityDto dto) throws BizException {
        if (repository.countByNameExcludeId(dto.getName(), null) > 0) {
            throw new BizException("能力包名称已存在,请更换后重试.");
        }
        AacpCapabilityPo insertPo = as(dto, AacpCapabilityPo.class);
        repository.save(insertPo);

        bindFuncIds(insertPo.getId(), dto.getFuncIds());
    }

    /**
     * 编辑能力包
     *
     * @param dto 编辑条件
     * @throws BizException 业务异常
     */
    @Transactional(rollbackFor = Exception.class)
    public void editAacpCapability(EditAacpCapabilityDto dto) throws BizException {
        if (repository.countByNameExcludeId(dto.getName(), dto.getId()) > 0) {
            throw new BizException("能力包名称已存在,请更换后重试.");
        }
        AacpCapabilityPo updatePo = repository.findById(dto.getId())
                .orElseThrow(() -> new BizException("更新失败,数据不存在或无权限访问."));

        assign(dto, updatePo);
        repository.save(updatePo);

        List<Long> existIds = capabilityFuncRepository.getFidsByCid(dto.getId());
        var idsDiff = new IdsDiff(existIds, dto.getFuncIds());

        if (idsDiff.hasAdd()) {
            var toAdd = idsDiff.getAddIds().stream()
                    .map(fid -> new AacpCapabilityFuncPo(dto.getId(), fid)).toList();
            capabilityFuncRepository.saveAll(toAdd);
        }

        if (idsDiff.hasRemove()) {
            capabilityFuncRepository.removeByCidAndFids(dto.getId(), idsDiff.getRemoveIds());
        }
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
        var vo = as(po, GetAacpCapabilityDetailsVo.class);

        //微函数能力包
        if (po.getKind() == 0) {
            vo.setFuncIds(capabilityFuncRepository.getFidsByCid(po.getId()));
        }

        return vo;
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
        capabilityFuncRepository.removeByCapabilityId(dto.getId());
        repository.deleteById(dto.getId());
    }

    /**
     * 绑定微函数到能力包
     */
    private void bindFuncIds(Long capabilityId, List<Long> funcIds) {
        if (funcIds == null || funcIds.isEmpty()) {
            return;
        }
        var pos = funcIds.stream()
                .map(fid -> new AacpCapabilityFuncPo(capabilityId, fid)).toList();
        capabilityFuncRepository.saveAll(pos);
    }

}
