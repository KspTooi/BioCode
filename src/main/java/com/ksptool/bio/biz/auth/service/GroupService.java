package com.ksptool.bio.biz.auth.service;


import com.ksptool.assembly.entity.exception.BizException;
import com.ksptool.assembly.entity.web.CommonIdDto;
import com.ksptool.assembly.entity.web.PageResult;
import com.ksptool.bio.biz.auth.common.RowScopes;
import com.ksptool.bio.biz.auth.model.GroupDeptPo;
import com.ksptool.bio.biz.auth.model.GroupMenuPo;
import com.ksptool.bio.biz.auth.model.GroupPermissionPo;
import com.ksptool.bio.biz.auth.model.group.GroupPo;
import com.ksptool.bio.biz.auth.model.group.dto.*;
import com.ksptool.bio.biz.auth.model.group.vo.GetGroupDetailsVo;
import com.ksptool.bio.biz.auth.model.group.vo.GetGroupListVo;
import com.ksptool.bio.biz.auth.model.group.vo.SimulateRsVo;
import com.ksptool.bio.biz.auth.repository.*;
import com.ksptool.bio.biz.core.common.IdsDiff;
import com.ksptool.bio.biz.core.common.SuperEntities;
import com.ksptool.bio.biz.core.common.Switch;
import com.ksptool.bio.biz.core.model.org.OrgPo;
import com.ksptool.bio.biz.core.repository.MenuRepository;
import com.ksptool.bio.biz.core.repository.OrgRepository;
import jakarta.persistence.Tuple;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Objects;

import static com.ksptool.bio.biz.core.common.TupleMapper.tupleAs;
import static com.ksptool.entities.Entities.as;
import static com.ksptool.entities.Entities.assign;

/**
 * @author KspTool
 * @since 1.1.1(A).80
 */
@Slf4j
@Service
public class GroupService {

    @Autowired
    private GroupRepository repository;

    @Autowired
    private PermissionRepository pRepository;

    @Autowired
    private UserGroupRepository ugRepository;

    @Autowired
    private GroupPermissionRepository gpRepository;

    @Autowired
    private GroupDeptRepository gdRepository;

    @Autowired
    private GroupMenuRepository gmRepository;

    @Autowired
    private OrgRepository orgRepository;

    /**
     * 获取用户组列表
     *
     * @param dto 获取用户组列表参数
     * @return 用户组列表
     */
    public PageResult<GetGroupListVo> getGroupList(GetGroupListDto dto) {
        Page<Tuple> page = repository.getGroupList(dto, dto.pageRequest());
        return PageResult.success(tupleAs(page.getContent(), GetGroupListVo.class), page.getTotalElements());
    }

    /**
     * 新增用户组
     *
     * @param dto 新增用户组参数
     * @throws BizException 用户组标识已存在
     */
    @Transactional(rollbackFor = Exception.class)
    public void addGroup(AddGroupDto dto) throws BizException {

        if (repository.countByCodeExcludeId(dto.getCode(), null) > 0) {
            throw new BizException("用户组编码 [" + dto.getCode() + "] 已被其他用户组占用,请更换编码后重试!");
        }

        //直接合并同类项
        var g = as(dto, GroupPo.class);
        g.setIsSystem(Switch.no());

        //保存用户组
        g = repository.save(g);
        var gId = g.getId();

        //处理GD关系 RS=指定组织时才需要处理 如果RS不是指定组织 则直接清空GD关系
        if (dto.getRowScope() == RowScopes.SPECIFIED_ORG) {

            var dPos = orgRepository.findAllById(dto.getDeptIds());
            var gdPos = dPos.stream().map(d -> {
                return new GroupDeptPo(gId, d.getId());
            }).toList();

            //这些所有的组织都必须属于同一个租户
            if (!dPos.isEmpty()) {

                var hSet = new HashSet<Long>();
                hSet.addAll(dPos.stream().map(d -> d.getRootId()).toList());

                if (hSet.size() > 1) {
                    throw new BizException("选择的多个组织机构不属于同一个租户!");
                }

            }

            //保存GD关系
            if (!gdPos.isEmpty()) {
                gdRepository.saveAll(gdPos);
            }

        }


    }

    /**
     * 编辑用户组
     *
     * @param dto 编辑用户组参数
     * @throws BizException 用户组不存在
     */
    @Transactional(rollbackFor = Exception.class)
    public void editGroup(EditGroupDto dto) throws BizException {

        GroupPo g = repository.findById(dto.getId()).orElseThrow(() -> new BizException("用户组不存在"));

        //处理系统内置用户组的更新逻辑
        if (g.isSystem()) {

            //内置用户组不可调整RS数据权限
            if (dto.getRowScope() != null && dto.getRowScope() != g.getRowScope()) {
                throw new BizException("内置用户组不允许调整RS数据权限！");
            }

            //内置用户组不可调整状态
            if (dto.getStatus() != null && !dto.getStatus().equals(g.getStatus())) {
                throw new BizException("内置用户组不允许调整状态！");
            }

            //内置用户组不可调整编码
            if (dto.getCode() != null && !dto.getCode().equals(g.getCode())) {
                throw new BizException("内置用户组不允许调整编码！");
            }

            //获取超级操作权限
            var sa = pRepository.getByCode(SuperEntities.PERMISSION.getCode());

            if (sa == null) {
                throw new BizException("系统异常，未能获取超级操作权限！");
            }

        }

        if (repository.countByCodeExcludeId(dto.getCode(), g.getId()) > 0) {
            throw new BizException("用户组编码 [" + dto.getCode() + "] 已被其他用户组占用,请更换编码后重试!");
        }

        //合并同类项
        assign(dto, g);

        //对比D关系的差异
        var gdIdsDiff = new IdsDiff(gdRepository.getDidsByGid(g.getId()), dto.getDeptIds());

        //处理GD的新增/删除关系 只有RS=指定组织时才需要处理 如果RS不是指定组织 则直接清空GD关系
        if (g.getRowScope() != RowScopes.SPECIFIED_ORG) {
            gdRepository.removeByGid(g.getId());
        }

        if (g.getRowScope() == RowScopes.SPECIFIED_ORG) {

            if (gdIdsDiff.hasAdd()) {

                var dPos = orgRepository.findAllById(gdIdsDiff.getAddIds());
                var gdPos = dPos.stream().map(d -> new GroupDeptPo(g.getId(), d.getId())).toList();

                var hSet = new HashSet<Long>();
                hSet.addAll(dPos.stream().map(d -> d.getRootId()).toList());

                //还要把这个G之前的GD也拉出来以防止之前的GD的RootId和新的GD的RootId不一致
                var oldDPos = orgRepository.findAllById(gdRepository.getDeptIdsByGroupId(g.getId()));
                hSet.addAll(oldDPos.stream().map(d -> d.getRootId()).toList());

                if (hSet.size() > 1) {
                    throw new BizException("选择的多个组织机构不属于同一个租户!");
                }

                gdRepository.saveAll(gdPos);
            }

            if (gdIdsDiff.hasRemove()) {
                gdRepository.removeByGidAndDids(g.getId(), gdIdsDiff.getRemoveIds());
            }

        }

    }

    /**
     * 获取用户组详情
     *
     * @param id 用户组ID
     * @return 用户组详情
     * @throws BizException 用户组不存在
     */
    public GetGroupDetailsVo getGroupDetails(long id) throws BizException {

        GroupPo po = repository.findById(id).orElseThrow(() -> new BizException("用户组不存在"));

        GetGroupDetailsVo vo = as(po, GetGroupDetailsVo.class);

        //如果数据权限为指定组织时，则需要获取组织列表
        if (po.getRowScope() == RowScopes.SPECIFIED_ORG) {
            var deptIds = gdRepository.getDeptIdsByGroupId(id);
            vo.setDeptIds(deptIds);
        }

        //获取该组拥有的GP
        vo.setPermissionIds(gpRepository.getPidsByGid(id));

        //获取该组拥有的GM
        vo.setMenuIds(gmRepository.getMidsByGid(id));
        return vo;
    }

    /**
     * 移除用户组
     *
     * @param dto 移除用户组参数
     * @throws BizException 用户组不存在
     */
    @Transactional(rollbackFor = Exception.class)
    public void removeGroup(CommonIdDto dto) throws BizException {

        var ids = dto.toIds();

        if (ids == null || ids.isEmpty()) {
            throw new BizException("用户组ID不能为空");
        }

        //查询存在的用户组记录
        var groups = repository.getGroupsByIds(ids);

        if (groups == null || groups.isEmpty()) {
            throw new BizException("一个或多个用户组不存在");
        }

        var safeRemoveIds = new ArrayList<Long>();
        String errorMessage = null;

        for (GroupPo group : groups) {

            //系统用户组无法删除
            if (group.getIsSystem() != null && group.getIsSystem() == 1) {
                errorMessage = "系统用户组无法删除";
                continue;
            }

            //用户组下面还有用户也不能删除
            var userCount = ugRepository.countUserByGroupId(group.getId());

            if (userCount > 0) {
                errorMessage = String.format("该用户组下有 %d 个用户，请先取消所有关联关系后再尝试移除", userCount);
                continue;
            }

            safeRemoveIds.add(group.getId());
        }

        //如果当前是单个删除模式且有错误 直接抛出异常
        if (!dto.isBatch() && errorMessage != null) {
            throw new BizException(errorMessage);
        }

        //当前是批量删除模式且没有任何一个用户组可以删除 则抛出异常
        if (dto.isBatch() && safeRemoveIds.isEmpty()) {
            throw new BizException("没有可以安全删除的用户组,请检查用户组状态或关联关系");
        }

        //删除该组下挂载的权限关系
        gpRepository.clearPermissionByGroupIds(safeRemoveIds);


        //执行静默删除
        repository.deleteAllById(safeRemoveIds);
    }


    /**
     * 更新组权限(GP)
     *
     * @param dto 更新组权限参数
     * @throws BizException 用户组不存在
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateGroupGp(UpdateGroupGpDto dto) throws BizException {
        var g = repository.findById(dto.getGroupId()).orElseThrow(() -> new BizException("用户组不存在"));

        //系统组不能把SA权限去除 (系统组指的是 ID:-1 且isSystem:1 的组)
        if (g.isSystem() && g.getId() == -1) {

            var sa = pRepository.getByCode(SuperEntities.PERMISSION.getCode());

            //检测前端传参是否把超级操作权限去除了 以避免用户解除超级组的SA权限导致超级组报废
            if (!dto.getPermissionIds().contains(sa.getId())) {
                throw new BizException("系统内置组不允许去除超级操作权限(SA)");
            }

        }


        //对比GP关系的差异
        var gpIdsDiff = new IdsDiff(gpRepository.getPidsByGid(g.getId()), dto.getPermissionIds());

        //处理GP的新增/删除关系
        if (gpIdsDiff.hasAdd()) {
            var gpPos = gpIdsDiff.getAddIds().stream().map(id -> new GroupPermissionPo(g.getId(), id)).toList();
            gpRepository.saveAll(gpPos);
        }

        if (gpIdsDiff.hasRemove()) {
            gpRepository.removeByGidAndPids(g.getId(), gpIdsDiff.getRemoveIds());
        }

    }

    /**
     * 更新组菜单
     *
     * @param dto 更新组菜单参数
     * @throws BizException 用户组不存在
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateGroupGm(UpdateGroupGmDto dto) throws BizException {

        var g = repository.findById(dto.getGroupId()).orElseThrow(() -> new BizException("用户组不存在"));

        if(g.isSystem()){
            throw new BizException("系统内置组不允许更新菜单(GM)");
        }

        //对比GM关系的差异
        var gmIdsDiff = new IdsDiff(gmRepository.getMidsByGid(g.getId()), dto.getMenuIds());

        //处理GM的新增/删除关系
        if (gmIdsDiff.hasAdd()) {
            var gmPos = gmIdsDiff.getAddIds().stream().map(id -> new GroupMenuPo(g.getId(), id)).toList();
            gmRepository.saveAll(gmPos);
        }

        if (gmIdsDiff.hasRemove()) {
            gmRepository.removeByGidAndMids(g.getId(), gmIdsDiff.getRemoveIds());
        }

    }


    /**
     * 模拟RS数据权限
     * <p>
     * 接收"模拟节点ID + 模拟RS等级",在不影响真实用户/组的前提下,
     * 实时计算并返回该等级下可见的组织节点ID集合。
     * <p>
     * 计算逻辑与 RsCalculator 完全对齐,以"虚拟用户"形式推导:
     * - kind=0/1(企业/子企业): 虚拟orgId=节点ID, 虚拟deptId=null
     * - kind=2/3(部门/班组): 虚拟deptId=节点ID, 虚拟orgId=节点的org_id
     *
     * @param dto 模拟参数
     * @return 模拟结果
     */
    public SimulateRsVo simulateRs(SimulateRsDto dto) throws Exception {

        RowScopes rsLevel = dto.getRsLevel();

        //校验RS等级 不支持指定组织(依赖组配置无法单点模拟)
        if (rsLevel == RowScopes.SPECIFIED_ORG) {
            throw new BizException("模拟器不支持 RS=60(指定组织)");
        }

        //校验RS等级合法值
        boolean validLevel = rsLevel == RowScopes.ALL || rsLevel == RowScopes.COMPANY_AND_SUBS
                || rsLevel == RowScopes.COMPANY_ONLY || rsLevel == RowScopes.DEPT_AND_SUBS
                || rsLevel == RowScopes.DEPT_ONLY || rsLevel == RowScopes.SELF_ONLY
                || rsLevel == RowScopes.DENY_ALL;
        if (!validLevel) {
            throw new BizException("非法的RS等级: " + rsLevel.getCode());
        }

        //获取当前登录用户的租户ID,用于隔离校验
        var session = SessionService.session();
        Long rootId = session.getRootId();

        //校验模拟节点存在且属于本租户
        OrgPo node = orgRepository.findById(dto.getOrgId())
                .orElseThrow(() -> new BizException("组织节点不存在"));

        if (!Objects.equals(node.getRootId(), rootId)) {
            throw new BizException("组织节点不属于当前租户");
        }

        //根据节点kind推导虚拟用户的orgId(公司)和deptId(部门)
        Long virtualOrgId = null;
        Long virtualDeptId = null;

        //kind=0(企业) 或 kind=1(子企业): 虚拟用户挂在该公司
        if (node.getKind() == 0 || node.getKind() == 1) {
            virtualOrgId = node.getId();
        }

        //kind>=2(部门/班组等): 虚拟用户挂在该部门,公司取org_id
        if (node.getKind() >= 2) {
            virtualDeptId = node.getId();
            virtualOrgId = node.getOrgId();
        }

        //构建结果VO基础信息
        var vo = new SimulateRsVo();
        vo.setRsLevel(rsLevel);
        vo.setOrgId(node.getId());
        vo.setNodeKind(node.getKind());
        vo.setAllMode(false);

        //rsLevel=全集团 SQL层直接放行租户全量,前端高亮全部节点
        if (rsLevel == RowScopes.ALL) {
            vo.setAllMode(true);
            vo.setVisibleOrgIds(new ArrayList<>());
            return vo;
        }

        //rsLevel=拒绝所有 orgIds永远不会匹配到任何ID
        if (rsLevel == RowScopes.DENY_ALL) {
            vo.setVisibleOrgIds(new ArrayList<>());
            return vo;
        }

        //rsLevel=仅本人 SQL层用creator_id过滤,组织树层面无命中节点
        if (rsLevel == RowScopes.SELF_ONLY) {
            vo.setVisibleOrgIds(new ArrayList<>());
            return vo;
        }

        var visibleIds = new HashSet<Long>();

        //rsLevel=本公司+下级公司 需要虚拟orgId
        if (rsLevel == RowScopes.COMPANY_AND_SUBS) {
            if (virtualOrgId == null) {
                vo.setVisibleOrgIds(new ArrayList<>());
                return vo;
            }
            var orgs = orgRepository.getChildByOrgId(virtualOrgId);
            for (var org : orgs) {
                visibleIds.add(org.getId());
            }
            visibleIds.add(virtualOrgId);
            vo.setVisibleOrgIds(new ArrayList<>(visibleIds));
            return vo;
        }

        //rsLevel=仅本公司(本公司直属部门,排除子公司及其下部门) 需要虚拟orgId
        if (rsLevel == RowScopes.COMPANY_ONLY) {
            if (virtualOrgId == null) {
                vo.setVisibleOrgIds(new ArrayList<>());
                return vo;
            }
            var orgs = orgRepository.getRowScope20OrgScopeListByOrgId(virtualOrgId);
            for (var org : orgs) {
                visibleIds.add(org.getId());
            }
            visibleIds.add(virtualOrgId);
            vo.setVisibleOrgIds(new ArrayList<>(visibleIds));
            return vo;
        }

        //rsLevel=本部门+下级部门 需要虚拟deptId
        if (rsLevel == RowScopes.DEPT_AND_SUBS) {
            if (virtualDeptId == null) {
                vo.setVisibleOrgIds(new ArrayList<>());
                return vo;
            }
            var orgs = orgRepository.getChildByOrgId(virtualDeptId);
            for (var org : orgs) {
                visibleIds.add(org.getId());
            }
            visibleIds.add(virtualDeptId);
            vo.setVisibleOrgIds(new ArrayList<>(visibleIds));
            return vo;
        }

        //rsLevel=仅本部门 需要虚拟deptId
        if (rsLevel == RowScopes.DEPT_ONLY) {
            if (virtualDeptId == null) {
                vo.setVisibleOrgIds(new ArrayList<>());
                return vo;
            }
            visibleIds.add(virtualDeptId);
            vo.setVisibleOrgIds(new ArrayList<>(visibleIds));
            return vo;
        }

        //兜底返回空集(理论上不会到达此分支,前面已覆盖全部合法值)
        vo.setVisibleOrgIds(new ArrayList<>());
        return vo;
    }


}
