package com.ksptool.bio.biz.qf.service;

import com.ksptool.assembly.entity.exception.BizException;
import com.ksptool.assembly.entity.web.CommonIdDto;
import com.ksptool.assembly.entity.web.PageResult;
import com.ksptool.bio.biz.qf.model.qfbizform.QfBizFormPo;
import com.ksptool.bio.biz.qf.model.qfbizform.dto.AddQfBizFormDto;
import com.ksptool.bio.biz.qf.model.qfbizform.dto.EditQfBizFormDto;
import com.ksptool.bio.biz.qf.model.qfbizform.dto.GetQfBizFormListDto;
import com.ksptool.bio.biz.qf.model.qfbizform.vo.GetQfBizFormDetailsVo;
import com.ksptool.bio.biz.qf.model.qfbizform.vo.GetQfBizFormListVo;
import com.ksptool.bio.biz.qf.repository.QfBizFormRepository;
import com.ksptool.bio.biz.qf.repository.QfTodoRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.ksptool.entities.Entities.as;
import static com.ksptool.entities.Entities.assign;

/**
 * 业务表单服务
 *
 * @author WangQingHua(603484930@qq.com)
 * @author KspTool(ksptool@outlook.com)
 * @license Proprietary
 * 版权所有 (c) 2026 KspTool及其贡献者保留所有权利。
 * 未经事先书面许可，严禁任何形式的复制或分发。
 * @since 2026-04-16
 */
@Service
public class QfBizFormService {

    @Autowired
    private QfBizFormRepository repository;

    @Autowired
    private QfTodoRepository qfTodoRepository;

    /**
     * 查询业务表单列表
     *
     * @param dto 查询条件
     * @return 查询结果
     */
    public PageResult<GetQfBizFormListVo> getBizFormList(GetQfBizFormListDto dto) {
        QfBizFormPo query = new QfBizFormPo();
        assign(dto, query);

        Page<QfBizFormPo> page = repository.getBizFormList(query, dto.pageRequest());
        if (page.isEmpty()) {
            return PageResult.successWithEmpty();
        }

        List<GetQfBizFormListVo> vos = as(page.getContent(), GetQfBizFormListVo.class);
        return PageResult.success(vos, (int) page.getTotalElements());
    }

    /**
     * 新增业务表单
     *
     * @param dto 新增条件
     */
    @Transactional(rollbackFor = Exception.class)
    public void addBizForm(AddQfBizFormDto dto) {
        QfBizFormPo insertPo = as(dto, QfBizFormPo.class);
        repository.save(insertPo);
    }

    /**
     * 编辑业务表单
     *
     * @param dto 编辑条件
     * @throws BizException 业务异常
     */
    @Transactional(rollbackFor = Exception.class)
    public void editBizForm(EditQfBizFormDto dto) throws BizException {
        QfBizFormPo updatePo = repository.findById(dto.getId())
                .orElseThrow(() -> new BizException("更新失败,数据不存在或无权限访问."));

        assign(dto, updatePo);
        repository.save(updatePo);
    }

    /**
     * 查询业务表单详情
     *
     * @param dto 查询条件
     * @return 查询结果
     * @throws BizException 业务异常
     */
    public GetQfBizFormDetailsVo getBizFormDetails(CommonIdDto dto) throws BizException {
        QfBizFormPo po = repository.findById(dto.getId())
                .orElseThrow(() -> new BizException("查询详情失败,数据不存在或无权限访问."));
        return as(po, GetQfBizFormDetailsVo.class);
    }

    /**
     * 删除业务表单
     *
     * @param dto 删除条件
     * @throws BizException 业务异常
     */
    @Transactional(rollbackFor = Exception.class)
    public void removeBizForm(CommonIdDto dto) throws BizException {
        if (dto.isBatch()) {
            repository.deleteAllById(dto.getIds());
            return;
        }

        //检查还有没有待办在用这个表单(只要有一个待办还没有完成就不能删除)
        var count = qfTodoRepository.countActiveTodyByBizFormId(dto.getId());
        
        if (count > 0) {
            throw new BizException("删除失败,该业务表单还有" + count + "个未完成的待办事项在使用.");
        }

        repository.deleteById(dto.getId());
    }

}
