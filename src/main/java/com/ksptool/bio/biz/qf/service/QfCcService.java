package com.ksptool.bio.biz.qf.service;

import com.ksptool.assembly.entity.exception.BizException;
import com.ksptool.assembly.entity.web.CommonIdDto;
import com.ksptool.assembly.entity.web.PageResult;
import com.ksptool.bio.biz.qf.model.qfcc.QfCcPo;
import com.ksptool.bio.biz.qf.model.qfcc.dto.AddQfCcDto;
import com.ksptool.bio.biz.qf.model.qfcc.dto.EditQfCcDto;
import com.ksptool.bio.biz.qf.model.qfcc.dto.GetQfCcListDto;
import com.ksptool.bio.biz.qf.model.qfcc.vo.GetQfCcDetailsVo;
import com.ksptool.bio.biz.qf.model.qfcc.vo.GetQfCcListVo;
import com.ksptool.bio.biz.qf.repository.QfCcRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.ksptool.entities.Entities.as;
import static com.ksptool.entities.Entities.assign;

/**
 * 抄送Service
 *
 * @author Akkarin(1075613357@qq.com)
 * @author KspTool(ksptool@outlook.com)
 * @license Proprietary
 * 版权所有 (c) 2026 KspTool及其贡献者保留所有权利。
 * 未经事先书面许可，严禁任何形式的复制或分发。
 * @since 2026-04-17
 */
@Service
public class QfCcService {

    @Autowired
    private QfCcRepository repository;

    /**
     * 查询抄送列表
     *
     * @param dto 查询条件
     * @return 查询结果
     */
    public PageResult<GetQfCcListVo> getQfCcList(GetQfCcListDto dto) {
        QfCcPo query = new QfCcPo();
        assign(dto, query);

        Page<QfCcPo> page = repository.getQfCcList(query, dto.pageRequest());
        if (page.isEmpty()) {
            return PageResult.successWithEmpty();
        }

        List<GetQfCcListVo> vos = as(page.getContent(), GetQfCcListVo.class);
        return PageResult.success(vos, (int) page.getTotalElements());
    }


    /**
     * 查询抄送详情
     *
     * @param dto 查询条件
     * @return 查询结果
     * @throws BizException 业务异常
     */
    public GetQfCcDetailsVo getQfCcDetails(CommonIdDto dto) throws BizException {
        QfCcPo po = repository.findById(dto.getId())
                .orElseThrow(() -> new BizException("查询详情失败,数据不存在或无权限访问."));
        return as(po, GetQfCcDetailsVo.class);
    }

    /**
     * 删除抄送
     *
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
