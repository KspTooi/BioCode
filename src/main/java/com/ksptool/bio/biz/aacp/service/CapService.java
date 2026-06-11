package com.ksptool.bio.biz.aacp.service;

import com.ksptool.assembly.entity.exception.BizException;
import com.ksptool.assembly.entity.web.CommonIdDto;
import com.ksptool.assembly.entity.web.PageResult;
import com.ksptool.bio.biz.aacp.model.AacpCapMicroFuncPo;
import com.ksptool.bio.biz.aacp.model.cap.AacpCapPo;
import com.ksptool.bio.biz.aacp.model.cap.dto.AddCapDto;
import com.ksptool.bio.biz.aacp.model.cap.dto.EditCapDto;
import com.ksptool.bio.biz.aacp.model.cap.dto.GetCapListDto;
import com.ksptool.bio.biz.aacp.model.cap.vo.GetCapDetailsVo;
import com.ksptool.bio.biz.aacp.model.cap.vo.GetCapListVo;
import com.ksptool.bio.biz.aacp.repository.CapMicroFuncRepository;
import com.ksptool.bio.biz.aacp.repository.CapRepository;
import com.ksptool.bio.biz.aacp.repository.McpCapRepository;
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
public class CapService {

    @Autowired
    private CapRepository repository;

    @Autowired
    private McpCapRepository mcpCapRepository;

    @Autowired
    private CapMicroFuncRepository capMicroFuncRepository;

    /**
     * 查询能力包列表
     *
     * @param dto 查询条件
     * @return 查询结果
     */
    public PageResult<GetCapListVo> getCapList(GetCapListDto dto) {
        AacpCapPo query = new AacpCapPo();
        assign(dto, query);

        Page<Tuple> page = repository.getCapList(query, dto.pageRequest());
        if (page.isEmpty()) {
            return PageResult.successWithEmpty();
        }

        List<GetCapListVo> vos = tupleAs(page.getContent(), GetCapListVo.class);
        return PageResult.success(vos, (int) page.getTotalElements());
    }

    /**
     * 新增能力包
     *
     * @param dto 新增条件
     * @throws BizException 业务异常
     */
    @Transactional(rollbackFor = Exception.class)
    public void addCap(AddCapDto dto) throws BizException {
        if (repository.countByNameExcludeId(dto.getName(), null) > 0) {
            throw new BizException("能力包名称已存在,请更换后重试.");
        }
        AacpCapPo insertPo = as(dto, AacpCapPo.class);
        repository.save(insertPo);

        var pos = dto.getFuncIds().stream()
                .map(fid -> new AacpCapMicroFuncPo(insertPo.getId(), fid)).toList();
        capMicroFuncRepository.saveAll(pos);
    }

    /**
     * 编辑能力包
     *
     * @param dto 编辑条件
     * @throws BizException 业务异常
     */
    @Transactional(rollbackFor = Exception.class)
    public void editCap(EditCapDto dto) throws BizException {
        if (repository.countByNameExcludeId(dto.getName(), dto.getId()) > 0) {
            throw new BizException("能力包名称已存在,请更换后重试.");
        }
        AacpCapPo updatePo = repository.findById(dto.getId())
                .orElseThrow(() -> new BizException("更新失败,数据不存在或无权限访问."));

        assign(dto, updatePo);
        repository.save(updatePo);

        List<Long> existIds = capMicroFuncRepository.getMicroFuncIdsByCapId(dto.getId());
        var idsDiff = new IdsDiff(existIds, dto.getFuncIds());

        if (idsDiff.hasAdd()) {
            var toAdd = idsDiff.getAddIds().stream()
                    .map(fid -> new AacpCapMicroFuncPo(dto.getId(), fid)).toList();
            capMicroFuncRepository.saveAll(toAdd);
        }

        if (idsDiff.hasRemove()) {
            capMicroFuncRepository.removeByCapIdAndMicroFuncIds(dto.getId(), idsDiff.getRemoveIds());
        }
    }

    /**
     * 查询能力包详情
     *
     * @param dto 查询条件
     * @return 查询结果
     * @throws BizException 业务异常
     */
    public GetCapDetailsVo getCapDetails(CommonIdDto dto) throws BizException {
        AacpCapPo po = repository.findById(dto.getId())
                .orElseThrow(() -> new BizException("查询详情失败,数据不存在或无权限访问."));
        var vo = as(po, GetCapDetailsVo.class);

        //微函数能力包
        if (po.getKind() == 0) {
            vo.setFuncIds(capMicroFuncRepository.getMicroFuncIdsByCapId(po.getId()));
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
    public void removeCap(CommonIdDto dto) throws BizException {
        if (dto.isBatch()) {
            throw new BizException("能力包不支持批量删除");
        }
        long refCount = mcpCapRepository.countByCapabilityId(dto.getId());
        if (refCount > 0) {
            throw new BizException("该能力包已被" + refCount + "台MCP服务器使用，无法删除");
        }
        capMicroFuncRepository.removeByCapId(dto.getId());
        repository.deleteById(dto.getId());
    }


}
