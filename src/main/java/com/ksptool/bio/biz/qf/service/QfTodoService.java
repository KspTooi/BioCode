package com.ksptool.bio.biz.qf.service;

import com.ksptool.assembly.entity.exception.BizException;
import com.ksptool.assembly.entity.web.CommonIdDto;
import com.ksptool.assembly.entity.web.PageResult;
import com.ksptool.bio.biz.qf.model.qftodo.QfTodoPo;
import com.ksptool.bio.biz.qf.model.qftodo.dto.AddQfTodoDto;
import com.ksptool.bio.biz.qf.model.qftodo.dto.ApproveQfTodoDto;
import com.ksptool.bio.biz.qf.model.qftodo.dto.EditQfTodoDto;
import com.ksptool.bio.biz.qf.model.qftodo.dto.GetQfTodoListDto;
import com.ksptool.bio.biz.qf.model.qftodo.vo.GetQfTodoDetailsVo;
import com.ksptool.bio.biz.qf.model.qftodo.vo.GetQfTodoListVo;
import com.ksptool.bio.biz.qf.repository.QfTodoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.ksptool.entities.Entities.as;
import static com.ksptool.entities.Entities.assign;


/**
 * 待办事项服务
 * 
 * @author WangQingHua(603484930@qq.com)
 * @author Akkarin(1075613357@qq.com)
 * @author (Ish)Yuumi(1144150092@qq.com)
 * @author KspTool(ksptool@outlook.com)
 * 
 * @since 2026-04-17
 * @license Apache License 2.0
 */
@Service
public class QfTodoService {

    @Autowired
    private QfTodoRepository repository;

    /**
     * 查询待办事项列表
     *
     * @param dto 查询条件
     * @return 查询结果
     */
    public PageResult<GetQfTodoListVo> getQfTodoList(GetQfTodoListDto dto) {
        QfTodoPo query = new QfTodoPo();
        assign(dto, query);

        Page<QfTodoPo> page = repository.getQfTodoList(query, dto.pageRequest());
        if (page.isEmpty()) {
            return PageResult.successWithEmpty();
        }

        List<GetQfTodoListVo> vos = as(page.getContent(), GetQfTodoListVo.class);
        return PageResult.success(vos, (int) page.getTotalElements());
    }

    /**
     * 新增待办事项
     *
     * @param dto 新增条件
     */
    @Transactional(rollbackFor = Exception.class)
    public void addQfTodo(AddQfTodoDto dto) {
        QfTodoPo insertPo = as(dto, QfTodoPo.class);
        repository.save(insertPo);
    }

    /**
     * 编辑待办事项
     *
     * @param dto 编辑条件
     * @throws BizException 业务异常
     */
    @Transactional(rollbackFor = Exception.class)
    public void editQfTodo(EditQfTodoDto dto) throws BizException {
        QfTodoPo updatePo = repository.findById(dto.getId())
                .orElseThrow(() -> new BizException("更新失败,数据不存在或无权限访问."));

        assign(dto, updatePo);
        repository.save(updatePo);
    }

    /**
     * 查询待办事项详情
     *
     * @param dto 查询条件
     * @return 查询结果
     * @throws BizException 业务异常
     */
    public GetQfTodoDetailsVo getQfTodoDetails(CommonIdDto dto) throws BizException {
        QfTodoPo po = repository.findById(dto.getId())
                .orElseThrow(() -> new BizException("查询详情失败,数据不存在或无权限访问."));
        return as(po, GetQfTodoDetailsVo.class);
    }

    /**
     * 删除待办事项
     *
     * @param dto 删除条件
     * @throws BizException 业务异常
     */
    @Transactional(rollbackFor = Exception.class)
    public void removeQfTodo(CommonIdDto dto) throws BizException {
        if (dto.isBatch()) {
            repository.deleteAllById(dto.getIds());
            return;
        }
        repository.deleteById(dto.getId());
    }

    /**
     * 审批待办事项
     *
     * @param dto 审批条件
     * @throws BizException 业务异常
     */
    @Transactional(rollbackFor = Exception.class)
    public void approveQfTodo(ApproveQfTodoDto dto) throws BizException {
        QfTodoPo updatePo = repository.findById(dto.getId())
                .orElseThrow(() -> new BizException("审批失败,数据不存在或无权限访问."));

        assign(dto, updatePo);
        repository.save(updatePo);
    }
}
