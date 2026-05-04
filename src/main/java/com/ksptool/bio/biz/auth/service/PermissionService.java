package com.ksptool.bio.biz.auth.service;

import com.ksptool.assembly.entity.exception.BizException;
import com.ksptool.assembly.entity.web.PageResult;
import com.ksptool.bio.biz.auth.model.permission.PermissionPo;
import com.ksptool.bio.biz.auth.model.permission.dto.AddPermissionDto;
import com.ksptool.bio.biz.auth.model.permission.dto.EditPermissionDto;
import com.ksptool.bio.biz.auth.model.permission.dto.GetPermissionListDto;
import com.ksptool.bio.biz.auth.model.permission.vo.GetPermissionDefinitionVo;
import com.ksptool.bio.biz.auth.model.permission.vo.GetPermissionDetailsVo;
import com.ksptool.bio.biz.auth.model.permission.vo.GetPermissionListVo;
import com.ksptool.bio.biz.auth.repository.GroupPermissionRepository;
import com.ksptool.bio.biz.auth.repository.PermissionRepository;
import com.ksptool.bio.biz.core.service.MenuService;
import com.ksptool.bio.biz.core.service.UserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.ksptool.entities.Entities.as;
import static com.ksptool.entities.Entities.assign;

/**
 * @author KspTool
 * @since 1.1.1(A).80
 */
@Service
public class PermissionService {

    @Autowired
    private PermissionRepository repository;

    @Autowired
    private GroupPermissionRepository gpRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private MenuService menuService;

    public List<GetPermissionDefinitionVo> getPermissionDefinition() {
        List<PermissionPo> pos = repository.findAll(Sort.by(Sort.Direction.DESC, "createTime"));
        return as(pos, GetPermissionDefinitionVo.class);
    }

    /**
     * 获取权限列表
     */
    public PageResult<GetPermissionListVo> getPermissionList(GetPermissionListDto dto) {
        // 创建分页对象，排序已在JPQL中指定
        Pageable pageable = dto.pageRequest();

        // 使用Repository的getPermissionList方法查询PO对象
        Page<PermissionPo> pagePos = repository.getPermissionList(dto, pageable);

        // 将PO转换为VO
        List<GetPermissionListVo> vos = as(pagePos.getContent(), GetPermissionListVo.class);

        // 返回结果
        return PageResult.success(vos, pagePos.getTotalElements());
    }

    /**
     * 获取权限详情
     */
    public GetPermissionDetailsVo getPermissionDetails(long id) throws BizException {

        PermissionPo permission = repository.findById(id)
                .orElseThrow(() -> new BizException("权限不存在"));

        GetPermissionDetailsVo vo = new GetPermissionDetailsVo();
        assign(permission, vo);
        return vo;
    }

    @Transactional(rollbackFor = Exception.class)
    public void addPermission(AddPermissionDto dto) throws BizException {
        if (StringUtils.isBlank(dto.getCode())) {
            throw new BizException("权限标识不能为空");
        }

        if (StringUtils.isBlank(dto.getName())) {
            throw new BizException("权限名称不能为空");
        }

        PermissionPo query = new PermissionPo();
        query.setCode(dto.getCode());

        Example<PermissionPo> example = Example.of(query);
        List<PermissionPo> existingPerms = repository.findAll(example);
        if (!existingPerms.isEmpty()) {
            throw new BizException("权限标识已存在");
        }

        PermissionPo permission = new PermissionPo();
        permission.setIsSystem(0);

        if (dto.getSeq() == null) {
            dto.setSeq(getNextSeq());
        }

        assign(dto, permission);
        repository.save(permission);
    }

    @Transactional(rollbackFor = Exception.class)
    public void editPermission(EditPermissionDto dto) throws BizException {
        if (StringUtils.isBlank(dto.getCode())) {
            throw new BizException("权限标识不能为空");
        }

        if (StringUtils.isBlank(dto.getName())) {
            throw new BizException("权限名称不能为空");
        }

        PermissionPo permission = repository.findById(dto.getId())
                .orElseThrow(() -> new BizException("权限不存在"));

        if (permission.getIsSystem() != null && permission.getIsSystem() == 1) {
            dto.setCode(permission.getCode());
            dto.setName(permission.getName());
        }

        PermissionPo query = new PermissionPo();
        query.setCode(dto.getCode());

        Example<PermissionPo> example = Example.of(query);
        List<PermissionPo> existingPerms = repository.findAll(example);
        PermissionPo existingPermByCode = existingPerms.isEmpty() ? null : existingPerms.getFirst();

        if (existingPermByCode != null && !existingPermByCode.getId().equals(dto.getId())) {
            throw new BizException("权限标识已存在");
        }

        if (dto.getSeq() == null) {
            dto.setSeq(getNextSeq());
        }

        assign(dto, permission);
        repository.save(permission);
    }

    /**
     * 删除权限
     */
    @Transactional(rollbackFor = Exception.class)
    public void removePermission(com.ksptool.assembly.entity.web.CommonIdDto dto) throws BizException {

        var ids = dto.toIds();

        if (ids == null || ids.isEmpty()) {
            throw new BizException("权限ID不能为空");
        }

        // 查询存在的权限记录
        var permissions = repository.findAllById(ids);

        if (permissions.isEmpty()) {
            throw new BizException("一个或多个权限记录不存在");
        }

        var safeRemoveIds = new ArrayList<Long>();
        String errorMessage = null;

        for (PermissionPo permission : permissions) {

            // 系统权限不允许删除
            if (permission.getIsSystem() != null && permission.getIsSystem() == 1) {
                errorMessage = "系统权限不允许删除";
                continue;
            }

            // 获取有多少用户组在使用该权限码
            var groupCount = gpRepository.countGroupPermissionByPermissionId(permission.getId());

            if (groupCount > 0) {
                errorMessage = String.format("权限 %s 已被 %d 个用户组使用，请先取消所有关联关系后再尝试移除", permission.getName(), groupCount);
                continue;
            }

            safeRemoveIds.add(permission.getId());
        }

        // 如果当前是单个删除模式且有错误 直接抛出异常
        if (!dto.isBatch() && errorMessage != null) {
            throw new BizException(errorMessage);
        }

        // 当前是批量删除模式且没有任何一个权限可以删除 则抛出异常
        if (dto.isBatch() && safeRemoveIds.isEmpty()) {
            throw new BizException("没有可以安全删除的权限,请检查系统权限标记或关联关系");
        }

        // 执行删除
        repository.deleteAllById(safeRemoveIds);

        //给拥有该权限的用户加版本
        for (var id : ids) {
            userService.increaseDvByPermissionId(id);
        }

        //清菜单缓存
        menuService.clearUserMenuTreeCache();
    }

    /**
     * 获取下一个可用的排序号
     */
    private Integer getNextSeq() {
        // 通过查询所有权限计算最大排序号
        List<PermissionPo> allPerms = repository.findAll();
        return allPerms.stream()
                .map(PermissionPo::getSeq)
                .filter(Objects::nonNull)
                .max(Integer::compareTo)
                .orElse(0) + 1;
    }

}
