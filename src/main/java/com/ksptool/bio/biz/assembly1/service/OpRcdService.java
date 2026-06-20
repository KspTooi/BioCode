package com.ksptool.bio.biz.assembly1.service;

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
import com.ksptool.bio.biz.assembly1.repository.OpRcdRepository;
import com.ksptool.bio.biz.assembly1.model.oprcd.OpRcdPo;
import com.ksptool.bio.biz.assembly1.model.oprcd.vo.GetOpRcdListVo;
import com.ksptool.bio.biz.assembly1.model.oprcd.dto.GetOpRcdListDto;
import com.ksptool.bio.biz.assembly1.model.oprcd.vo.GetOpRcdDetailsVo;
import com.ksptool.bio.biz.assembly1.model.oprcd.dto.EditOpRcdDto;
import com.ksptool.bio.biz.assembly1.model.oprcd.dto.AddOpRcdDto;


@Service
public class OpRcdService {

    @Autowired
    private OpRcdRepository repository;

    /**
     * 查询输出方案执行记录列表
     * @param dto 查询条件
     * @return 查询结果
     */
    public PageResult<GetOpRcdListVo> getOpRcdList(GetOpRcdListDto dto){
        OpRcdPo query = new OpRcdPo();
        assign(dto,query);

        Page<OpRcdPo> page = repository.getOpRcdList(query, dto.pageRequest());
        if (page.isEmpty()) {
            return PageResult.successWithEmpty();
        }

        List<GetOpRcdListVo> vos = as(page.getContent(), GetOpRcdListVo.class);
        return PageResult.success(vos, (int) page.getTotalElements());
    }

    /**
     * 新增输出方案执行记录
     * @param dto 新增条件
     */
    @Transactional(rollbackFor = Exception.class)
    public void addOpRcd(AddOpRcdDto dto){
        OpRcdPo insertPo = as(dto,OpRcdPo.class);
        repository.save(insertPo);
    }

    /**
     * 编辑输出方案执行记录
     * @param dto 编辑条件
     * @throws BizException 业务异常
     */
    @Transactional(rollbackFor = Exception.class)
    public void editOpRcd(EditOpRcdDto dto) throws BizException {
        OpRcdPo updatePo = repository.findById(dto.getId())
            .orElseThrow(()-> new BizException("更新失败,数据不存在或无权限访问."));

        assign(dto,updatePo);
        repository.save(updatePo);
    }

    /**
     * 查询输出方案执行记录详情
     * @param dto 查询条件
     * @return 查询结果
     * @throws BizException 业务异常
     */
    public GetOpRcdDetailsVo getOpRcdDetails(CommonIdDto dto) throws BizException {
        OpRcdPo po = repository.findById(dto.getId())
            .orElseThrow(()-> new BizException("查询详情失败,数据不存在或无权限访问."));
        return as(po,GetOpRcdDetailsVo.class);
    }

    /**
     * 删除输出方案执行记录
     * @param dto 删除条件
     * @throws BizException 业务异常
     */
    @Transactional(rollbackFor = Exception.class)
    public void removeOpRcd(CommonIdDto dto) throws BizException {
        if (dto.isBatch()) {
            repository.deleteAllById(dto.getIds());
            return;
        }
        repository.deleteById(dto.getId());
    }

}
