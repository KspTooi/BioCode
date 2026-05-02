package com.ksptool.bio.biz.document.service;

import com.ksptool.assembly.entity.exception.BizException;
import com.ksptool.assembly.entity.web.CommonIdDto;
import com.ksptool.assembly.entity.web.PageResult;
import com.ksptool.bio.biz.auth.common.aop.RowScope;
import com.ksptool.bio.biz.document.model.prompt.PromptPo;
import com.ksptool.bio.biz.document.model.prompt.dto.AddPromptDto;
import com.ksptool.bio.biz.document.model.prompt.dto.CompilePromptDto;
import com.ksptool.bio.biz.document.model.prompt.dto.EditPromptDto;
import com.ksptool.bio.biz.document.model.prompt.dto.GetPromptListDto;
import com.ksptool.bio.biz.document.model.prompt.vo.GetPromptDetailsVo;
import com.ksptool.bio.biz.document.model.prompt.vo.GetPromptListVo;
import com.ksptool.bio.biz.document.repository.PromptRepository;
import com.ksptool.text.PreparedPrompt;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.ksptool.entities.Entities.as;
import static com.ksptool.entities.Entities.assign;


@Service
@RowScope
public class PromptService {

    @Autowired
    private PromptRepository repository;

    /**
     * 查询提示词列表
     *
     * @param dto 查询条件
     * @return 查询结果
     */
    public PageResult<GetPromptListVo> getPromptList(GetPromptListDto dto) {

        PromptPo query = new PromptPo();
        assign(dto, query);

        Page<PromptPo> page = repository.getPromptList(dto, dto.pageRequest());
        if (page.isEmpty()) {
            return PageResult.successWithEmpty();
        }

        List<GetPromptListVo> vos = new ArrayList<>();

        //计算字符数
        for (PromptPo po : page.getContent()) {
            var vo = as(po, GetPromptListVo.class);

            vo.setCharCount(0);

            if (po.getContent() != null) {
                vo.setCharCount(po.getContent().length());
            }

            vos.add(vo);
        }

        return PageResult.success(vos, (int) page.getTotalElements());
    }

    /**
     * 新增提示词
     *
     * @param dto 新增条件
     */
    @Transactional(rollbackFor = Exception.class)
    public void addPrompt(AddPromptDto dto) throws BizException {
        PromptPo insertPo = as(dto, PromptPo.class);

        //校验名称是否唯一
        if (repository.countByNameExcludeId(insertPo.getName(), null) > 0) {
            throw new BizException("名称已存在:[" + insertPo.getName() + "]");
        }

        insertPo.setParamCount(0);

        //计算参数数量
        if (dto.getContent() != null) {
            PreparedPrompt prompt = new PreparedPrompt(dto.getContent());
            insertPo.setParamCount(prompt.getUnsetParameters().length);
        }
        insertPo.setVersion(1);
        repository.save(insertPo);
    }

    /**
     * 编辑提示词
     *
     * @param dto 编辑条件
     * @throws BizException 业务异常
     */
    @Transactional(rollbackFor = Exception.class)
    public void editPrompt(EditPromptDto dto) throws BizException {

        PromptPo updatePo = repository.findById(dto.getId())
                .orElseThrow(() -> new BizException("更新失败,数据不存在或无权限访问."));

        var oldContent = updatePo.getContent();
        var oldParamCount = updatePo.getParamCount();

        assign(dto, updatePo);

        updatePo.setParamCount(0);
        //计算参数数量
        if (dto.getContent() != null) {
            PreparedPrompt prompt = new PreparedPrompt(dto.getContent());
            updatePo.setParamCount(prompt.getUnsetParameters().length);
        }

        //版本号+1
        var oldVersion = updatePo.getVersion();
        var newVersion = oldVersion + 1;
        updatePo.setVersion(newVersion);
        repository.save(updatePo);

        //创建历史版本
        PromptPo historyPo = as(updatePo, PromptPo.class);
        historyPo.setId(null);
        historyPo.setVersion(oldVersion);
        historyPo.setContent(oldContent);
        historyPo.setParamCount(oldParamCount);
        repository.save(historyPo);
    }

    /**
     * 查询提示词详情
     *
     * @param dto 查询条件
     * @return 查询结果
     * @throws BizException 业务异常
     */
    public GetPromptDetailsVo getPromptDetails(CommonIdDto dto) throws BizException {
        PromptPo po = repository.findById(dto.getId())
                .orElseThrow(() -> new BizException("查询详情失败,数据不存在或无权限访问."));

        var vo = as(po, GetPromptDetailsVo.class);

        vo.setParamSlots(new ArrayList<>());

        //如果内容不为空则获取参数槽位
        if (StringUtils.isNotBlank(po.getContent())) {
            PreparedPrompt prompt = new PreparedPrompt(po.getContent());
            vo.setParamSlots(Arrays.asList(prompt.getUnsetParameters()));
        }

        return vo;
    }

    /**
     * 删除提示词
     *
     * @param dto 删除条件
     * @throws BizException 业务异常
     */
    @Transactional(rollbackFor = Exception.class)
    public void removePrompt(CommonIdDto dto) throws BizException {
        if (dto.isBatch()) {
            repository.deleteAllById(dto.getIds());
            return;
        }
        repository.deleteById(dto.getId());
    }

    /**
     * 编译提示词
     *
     * @param dto 编译条件
     * @return 编译结果
     * @throws BizException 业务异常
     */
    public String compilePrompt(CompilePromptDto dto) throws BizException {

        PromptPo po = repository.findById(dto.getId())
                .orElseThrow(() -> new BizException("编译失败,数据不存在或无权限访问."));

        if (StringUtils.isBlank(po.getContent())) {
            throw new BizException("编译失败,内容不能为空.");
        }

        PreparedPrompt prompt = new PreparedPrompt(po.getContent());

        if (prompt.getUnsetParameters().length < 1) {
            throw new BizException("编译失败,该提示词没有参数槽位.");
        }

        prompt.setParameters(dto.getParams());
        return prompt.execute();
    }

}
