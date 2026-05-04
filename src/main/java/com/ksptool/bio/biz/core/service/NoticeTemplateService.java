package com.ksptool.bio.biz.core.service;

import com.ksptool.assembly.entity.exception.BizException;
import com.ksptool.assembly.entity.web.CommonIdDto;
import com.ksptool.assembly.entity.web.PageResult;
import com.ksptool.bio.biz.core.model.noticetemplate.NoticeTemplatePo;
import com.ksptool.bio.biz.core.model.noticetemplate.dto.AddNoticeTemplateDto;
import com.ksptool.bio.biz.core.model.noticetemplate.dto.EditNoticeTemplateDto;
import com.ksptool.bio.biz.core.model.noticetemplate.dto.GetNoticeTemplateListDto;
import com.ksptool.bio.biz.core.model.noticetemplate.vo.GetNoticeTemplateDetailsVo;
import com.ksptool.bio.biz.core.model.noticetemplate.vo.GetNoticeTemplateListVo;
import com.ksptool.bio.biz.core.repository.NoticeTemplateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.ksptool.entities.Entities.as;
import static com.ksptool.entities.Entities.assign;

/**
 * @author KspTooi
 * @since 1.2.8(H).1
 */
@Service
public class NoticeTemplateService {

    @Autowired
    private NoticeTemplateRepository repository;

    /**
     * 查询通知模板列表
     *
     * @param dto 查询条件
     * @return 通知模板列表
     */
    public PageResult<GetNoticeTemplateListVo> getNoticeTemplateList(GetNoticeTemplateListDto dto) {
        NoticeTemplatePo query = new NoticeTemplatePo();
        assign(dto, query);

        Page<NoticeTemplatePo> page = repository.getNoticeTemplateList(query, dto.pageRequest());
        if (page.isEmpty()) {
            return PageResult.successWithEmpty();
        }

        List<GetNoticeTemplateListVo> vos = as(page.getContent(), GetNoticeTemplateListVo.class);
        return PageResult.success(vos, (int) page.getTotalElements());
    }

    /**
     * 新增通知模板
     *
     * @param dto 新增模板
     * @throws BizException 业务异常
     */
    @Transactional(rollbackFor = Exception.class)
    public void addNoticeTemplate(AddNoticeTemplateDto dto) throws BizException {
        NoticeTemplatePo insertPo = as(dto, NoticeTemplatePo.class);

        //查询code是否被占用
        var existPo = repository.countNoticeTemplateByCode(dto.getCode());
        if (existPo > 0) {
            throw new BizException("模板标识已存在:[" + dto.getCode() + "]");
        }

        repository.save(insertPo);
    }

    /**
     * 编辑通知模板
     *
     * @param dto 编辑模板
     * @throws BizException 业务异常
     */
    @Transactional(rollbackFor = Exception.class)
    public void editNoticeTemplate(EditNoticeTemplateDto dto) throws BizException {
        NoticeTemplatePo updatePo = repository.findById(dto.getId())
                .orElseThrow(() -> new BizException("更新失败,数据不存在或无权限访问."));


        //查询code是否被占用
        var existPo = repository.countNoticeTemplateByCodeExcludeId(dto.getCode(), updatePo.getId());

        if (existPo > 0) {
            throw new BizException("模板标识已存在:[" + dto.getCode() + "]");
        }

        assign(dto, updatePo);
        repository.save(updatePo);
    }

    /**
     * 查询通知模板详情
     *
     * @param dto 查询条件
     * @return 通知模板详情
     * @throws BizException 业务异常
     */
    public GetNoticeTemplateDetailsVo getNoticeTemplateDetails(CommonIdDto dto) throws BizException {
        NoticeTemplatePo po = repository.findById(dto.getId())
                .orElseThrow(() -> new BizException("查询详情失败,数据不存在或无权限访问."));
        return as(po, GetNoticeTemplateDetailsVo.class);
    }

    /**
     * 删除通知模板
     *
     * @param dto 删除条件
     * @throws BizException 业务异常
     */
    @Transactional(rollbackFor = Exception.class)
    public void removeNoticeTemplate(CommonIdDto dto) throws BizException {
        if (dto.isBatch()) {
            repository.deleteAllById(dto.getIds());
            return;
        }
        repository.deleteById(dto.getId());
    }

}