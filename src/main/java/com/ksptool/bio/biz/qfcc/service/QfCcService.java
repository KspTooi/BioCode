package com.ksptool.bio.biz.qfcc.service;

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
import com.ksptool.bio.biz.qfcc.repository.QfCcRepository;
import com.ksptool.bio.biz.qfcc.model.QfCcPo;
import com.ksptool.bio.biz.qfcc.model.vo.GetQfCcListVo;
import com.ksptool.bio.biz.qfcc.model.dto.GetQfCcListDto;
import com.ksptool.bio.biz.qfcc.model.vo.GetQfCcDetailsVo;
import com.ksptool.bio.biz.qfcc.model.dto.EditQfCcDto;
import com.ksptool.bio.biz.qfcc.model.dto.AddQfCcDto;


@Service
public class QfCcService {

    @Autowired
    private QfCcRepository repository;

    /**
     * 查询抄送列表
     * @param dto 查询条件
     * @return 查询结果
     */
    public PageResult<GetQfCcListVo> getQfCcList(GetQfCcListDto dto){
        QfCcPo query = new QfCcPo();
        assign(dto,query);

        Page<QfCcPo> page = repository.getQfCcList(query, dto.pageRequest());
        if (page.isEmpty()) {
            return PageResult.successWithEmpty();
        }

        List<GetQfCcListVo> vos = as(page.getContent(), GetQfCcListVo.class);
        return PageResult.success(vos, (int) page.getTotalElements());
    }

    /**
     * 新增抄送
     * @param dto 新增条件
     */
    @Transactional(rollbackFor = Exception.class)
    public void addQfCc(AddQfCcDto dto){
        QfCcPo insertPo = as(dto,QfCcPo.class);
        repository.save(insertPo);
    }

    /**
     * 编辑抄送
     * @param dto 编辑条件
     * @throws BizException 业务异常
     */
    @Transactional(rollbackFor = Exception.class)
    public void editQfCc(EditQfCcDto dto) throws BizException {
        QfCcPo updatePo = repository.findById(dto.getId())
            .orElseThrow(()-> new BizException("更新失败,数据不存在或无权限访问."));

        assign(dto,updatePo);
        repository.save(updatePo);
    }

    /**
     * 查询抄送详情
     * @param dto 查询条件
     * @return 查询结果
     * @throws BizException 业务异常
     */
    public GetQfCcDetailsVo getQfCcDetails(CommonIdDto dto) throws BizException {
        QfCcPo po = repository.findById(dto.getId())
            .orElseThrow(()-> new BizException("查询详情失败,数据不存在或无权限访问."));
        return as(po,GetQfCcDetailsVo.class);
    }

    /**
     * 删除抄送
     * @param dto 删除条件
     * @throws BizException 业务异常
     */
    @Transactional(rollbackFor = Exception.class)
    public void removeQfCc(CommonIdDto dto) throws BizException {
        if (dto.isBatch()) {
            repository.deleteAllById(dto.getIds());
            return;
        }
        repository.deleteById(dto.getId());
    }

}
