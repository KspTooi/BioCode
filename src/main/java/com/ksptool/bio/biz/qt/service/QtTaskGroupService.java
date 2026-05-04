package com.ksptool.bio.biz.qt.service;

import com.ksptool.assembly.entity.exception.BizException;
import com.ksptool.assembly.entity.web.CommonIdDto;
import com.ksptool.assembly.entity.web.PageResult;
import com.ksptool.bio.biz.qt.model.qttaskgroup.QtTaskGroupPo;
import com.ksptool.bio.biz.qt.model.qttaskgroup.dto.AddQtTaskGroupDto;
import com.ksptool.bio.biz.qt.model.qttaskgroup.dto.EditQtTaskGroupDto;
import com.ksptool.bio.biz.qt.model.qttaskgroup.dto.GetQtTaskGroupListDto;
import com.ksptool.bio.biz.qt.model.qttaskgroup.vo.GetQtTaskGroupDetailsVo;
import com.ksptool.bio.biz.qt.model.qttaskgroup.vo.GetQtTaskGroupListVo;
import com.ksptool.bio.biz.qt.repository.QtTaskGroupRepository;
import com.ksptool.bio.biz.qt.repository.QtTaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

import static com.ksptool.entities.Entities.as;
import static com.ksptool.entities.Entities.assign;


/**
 * QF任务分组服务
 *
 * @author KspTool(ksptool@outlook.com)
 * @license Apache License 2.0
 * @since 1.6.1(A).1
 */
@Service
public class QtTaskGroupService {

    @Autowired
    private QtTaskGroupRepository repository;

    @Autowired
    private QtTaskRepository taskRepository;

    /**
     * 查询任务分组列表
     *
     * @param dto 查询条件
     * @return 任务分组列表
     */
    public PageResult<GetQtTaskGroupListVo> getQtTaskGroupList(GetQtTaskGroupListDto dto) {
        QtTaskGroupPo query = new QtTaskGroupPo();
        assign(dto, query);

        Page<QtTaskGroupPo> page = repository.getQtTaskGroupList(query, dto.pageRequest());
        if (page.isEmpty()) {
            return PageResult.successWithEmpty();
        }

        List<GetQtTaskGroupListVo> vos = as(page.getContent(), GetQtTaskGroupListVo.class);
        return PageResult.success(vos, (int) page.getTotalElements());
    }

    /**
     * 新增任务分组
     *
     * @param dto 新增任务分组信息
     */
    @Transactional(rollbackFor = Exception.class)
    public void addQtTaskGroup(AddQtTaskGroupDto dto) throws BizException {


        //查询名称是否被占用
        var existPo = repository.countByName(dto.getName());
        if (existPo > 0) {
            throw new BizException("任务分组名称已存在:[" + dto.getName() + "]");
        }

        QtTaskGroupPo insertPo = as(dto, QtTaskGroupPo.class);
        repository.save(insertPo);
    }

    /**
     * 编辑任务分组
     *
     * @param dto 编辑任务分组信息
     */
    @Transactional(rollbackFor = Exception.class)
    public void editQtTaskGroup(EditQtTaskGroupDto dto) throws BizException {
        QtTaskGroupPo updatePo = repository.findById(dto.getId())
                .orElseThrow(() -> new BizException("更新失败,数据不存在或无权限访问."));

        //查询名称是否被占用
        var existPo = repository.countByNameExcludeId(dto.getName(), updatePo.getId());
        if (existPo > 0) {
            throw new BizException("任务分组名称已存在:[" + dto.getName() + "]");
        }

        assign(dto, updatePo);

        //如果分组改名 需要同步修改任务分组名
        if (!updatePo.getName().equals(dto.getName())) {
            taskRepository.updateGroupNameByGroupId(updatePo.getId(), dto.getName());
        }

        repository.save(updatePo);
    }

    /**
     * 查询任务分组详情
     *
     * @param dto 查询条件
     * @return 任务分组详情
     */
    public GetQtTaskGroupDetailsVo getQtTaskGroupDetails(CommonIdDto dto) throws BizException {
        QtTaskGroupPo po = repository.findById(dto.getId())
                .orElseThrow(() -> new BizException("查询详情失败,数据不存在或无权限访问."));
        return as(po, GetQtTaskGroupDetailsVo.class);
    }

    /**
     * 删除任务分组
     *
     * @param dto 删除条件
     */
    @Transactional(rollbackFor = Exception.class)
    public void removeQtTaskGroup(CommonIdDto dto) throws BizException {

        var ids = dto.toIds();
        var safeRemoveIds = new ArrayList<Long>();

        for (Long id : ids) {

            if (taskRepository.countByGroupId(id) > 0) {
                continue;
            }

            safeRemoveIds.add(id);
        }

        if (safeRemoveIds.isEmpty()) {
            throw new BizException("删除失败,所选任务分组下有任务,请先移除任务后再删除.");
        }

        repository.deleteAllById(safeRemoveIds);
    }

}