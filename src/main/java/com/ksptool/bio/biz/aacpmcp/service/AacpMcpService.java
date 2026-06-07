package com.ksptool.bio.biz.aacpmcp.service;

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
import com.ksptool.bio.biz.aacpmcp.repository.AacpMcpRepository;
import com.ksptool.bio.biz.aacpmcp.model.AacpMcpPo;
import com.ksptool.bio.biz.aacpmcp.model.vo.GetAacpMcpListVo;
import com.ksptool.bio.biz.aacpmcp.model.dto.GetAacpMcpListDto;
import com.ksptool.bio.biz.aacpmcp.model.vo.GetAacpMcpDetailsVo;
import com.ksptool.bio.biz.aacpmcp.model.dto.EditAacpMcpDto;
import com.ksptool.bio.biz.aacpmcp.model.dto.AddAacpMcpDto;


@Service
public class AacpMcpService {

    @Autowired
    private AacpMcpRepository repository;

    /**
     * 查询MCP服务器列表
     * @param dto 查询条件
     * @return 查询结果
     */
    public PageResult<GetAacpMcpListVo> getAacpMcpList(GetAacpMcpListDto dto){
        AacpMcpPo query = new AacpMcpPo();
        assign(dto,query);

        Page<AacpMcpPo> page = repository.getAacpMcpList(query, dto.pageRequest());
        if (page.isEmpty()) {
            return PageResult.successWithEmpty();
        }

        List<GetAacpMcpListVo> vos = as(page.getContent(), GetAacpMcpListVo.class);
        return PageResult.success(vos, (int) page.getTotalElements());
    }

    /**
     * 新增MCP服务器
     * @param dto 新增条件
     */
    @Transactional(rollbackFor = Exception.class)
    public void addAacpMcp(AddAacpMcpDto dto){
        AacpMcpPo insertPo = as(dto,AacpMcpPo.class);
        repository.save(insertPo);
    }

    /**
     * 编辑MCP服务器
     * @param dto 编辑条件
     * @throws BizException 业务异常
     */
    @Transactional(rollbackFor = Exception.class)
    public void editAacpMcp(EditAacpMcpDto dto) throws BizException {
        AacpMcpPo updatePo = repository.findById(dto.getId())
            .orElseThrow(()-> new BizException("更新失败,数据不存在或无权限访问."));

        assign(dto,updatePo);
        repository.save(updatePo);
    }

    /**
     * 查询MCP服务器详情
     * @param dto 查询条件
     * @return 查询结果
     * @throws BizException 业务异常
     */
    public GetAacpMcpDetailsVo getAacpMcpDetails(CommonIdDto dto) throws BizException {
        AacpMcpPo po = repository.findById(dto.getId())
            .orElseThrow(()-> new BizException("查询详情失败,数据不存在或无权限访问."));
        return as(po,GetAacpMcpDetailsVo.class);
    }

    /**
     * 删除MCP服务器
     * @param dto 删除条件
     * @throws BizException 业务异常
     */
    @Transactional(rollbackFor = Exception.class)
    public void removeAacpMcp(CommonIdDto dto) throws BizException {
        if (dto.isBatch()) {
            repository.deleteAllById(dto.getIds());
            return;
        }
        repository.deleteById(dto.getId());
    }

}
