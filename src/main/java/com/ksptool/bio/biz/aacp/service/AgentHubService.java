package com.ksptool.bio.biz.aacp.service;

import com.ksptool.assembly.entity.exception.BizException;
import com.ksptool.assembly.entity.web.CommonIdDto;
import com.ksptool.assembly.entity.web.PageResult;
import com.ksptool.bio.biz.aacp.model.AacpAgentHubCapPo;
import com.ksptool.bio.biz.aacp.model.agenthub.AacpAgentHubPo;
import com.ksptool.bio.biz.aacp.model.agenthub.dto.AddAgentHubDto;
import com.ksptool.bio.biz.aacp.model.agenthub.dto.EditAgentHubDto;
import com.ksptool.bio.biz.aacp.model.agenthub.dto.GetAgentHubListDto;
import com.ksptool.bio.biz.aacp.model.agenthub.vo.GetAgentHubDetailsVo;
import com.ksptool.bio.biz.aacp.model.agenthub.vo.GetAgentHubListVo;
import com.ksptool.bio.biz.aacp.repository.AgentHubRepository;
import com.ksptool.bio.biz.aacp.repository.AgentHubCapRepository;
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
public class AgentHubService {

    @Autowired
    private AgentHubRepository repository;

    @Autowired
    private AgentHubCapRepository agentHubCapRepository;

    /**
     * 查询智能体枢纽列表
     *
     * @param dto 查询条件
     * @return 查询结果
     */
    public PageResult<GetAgentHubListVo> getAgentHubList(GetAgentHubListDto dto) {
        AacpAgentHubPo query = new AacpAgentHubPo();
        assign(dto, query);

        Page<Tuple> page = repository.getAgentHubList(query, dto.pageRequest());
        if (page.isEmpty()) {
            return PageResult.successWithEmpty();
        }

        List<GetAgentHubListVo> vos = tupleAs(page.getContent(), GetAgentHubListVo.class);
        return PageResult.success(vos, (int) page.getTotalElements());
    }

    /**
     * 新增智能体枢纽
     *
     * @param dto 新增条件
     */
    @Transactional(rollbackFor = Exception.class)
    public void addAgentHub(AddAgentHubDto dto) throws BizException {
        if (repository.countByCodeExcludeId(dto.getCode(), null) > 0) {
            throw new BizException("唯一编码已存在,请更换后重试.");
        }
        AacpAgentHubPo insertPo = as(dto, AacpAgentHubPo.class);
        repository.save(insertPo);

        var cids = dto.getCapabilityIds();
        if (cids != null && !cids.isEmpty()) {
            var pos = cids.stream()
                    .map(cid -> new AacpAgentHubCapPo(insertPo.getId(), cid)).toList();
            agentHubCapRepository.saveAll(pos);
        }
    }

    /**
     * 编辑智能体枢纽
     *
     * @param dto 编辑条件
     * @throws BizException 业务异常
     */
    @Transactional(rollbackFor = Exception.class)
    public void editAgentHub(EditAgentHubDto dto) throws BizException {
        if (repository.countByCodeExcludeId(dto.getCode(), dto.getId()) > 0) {
            throw new BizException("唯一编码已存在,请更换后重试.");
        }
        AacpAgentHubPo updatePo = repository.findById(dto.getId())
                .orElseThrow(() -> new BizException("更新失败,数据不存在或无权限访问."));

        assign(dto, updatePo);
        repository.save(updatePo);

        List<Long> existIds = agentHubCapRepository.getCapIdsByHubId(dto.getId());
        var idsDiff = new IdsDiff(existIds, dto.getCapabilityIds());

        if (idsDiff.hasAdd()) {
            var toAdd = idsDiff.getAddIds().stream()
                    .map(cid -> new AacpAgentHubCapPo(dto.getId(), cid)).toList();
            agentHubCapRepository.saveAll(toAdd);
        }

        if (idsDiff.hasRemove()) {
            agentHubCapRepository.removeByHubIdAndCapIds(dto.getId(), idsDiff.getRemoveIds());
        }
    }

    /**
     * 查询智能体枢纽详情
     *
     * @param dto 查询条件
     * @return 查询结果
     * @throws BizException 业务异常
     */
    public GetAgentHubDetailsVo getAgentHubDetails(CommonIdDto dto) throws BizException {
        AacpAgentHubPo po = repository.findById(dto.getId())
                .orElseThrow(() -> new BizException("查询详情失败,数据不存在或无权限访问."));
        GetAgentHubDetailsVo vo = as(po, GetAgentHubDetailsVo.class);
        vo.setCapabilityIds(agentHubCapRepository.getCapIdsByHubId(dto.getId()));
        return vo;
    }

    /**
     * 删除智能体枢纽
     *
     * @param dto 删除条件
     * @throws BizException 业务异常
     */
    @Transactional(rollbackFor = Exception.class)
    public void removeAgentHub(CommonIdDto dto) throws BizException {
        if (dto.isBatch()) {
            repository.deleteAllById(dto.getIds());
            return;
        }
        agentHubCapRepository.removeByHubId(dto.getId());
        repository.deleteById(dto.getId());
    }

}
