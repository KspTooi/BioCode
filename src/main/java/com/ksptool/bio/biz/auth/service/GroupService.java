package com.ksptool.bio.biz.auth.service;


import com.ksptool.assembly.entity.exception.BizException;
import com.ksptool.assembly.entity.web.CommonIdDto;
import com.ksptool.assembly.entity.web.PageResult;
import com.ksptool.bio.biz.auth.model.GroupDeptPo;
import com.ksptool.bio.biz.auth.model.GroupMenuPo;
import com.ksptool.bio.biz.auth.model.GroupPermissionPo;
import com.ksptool.bio.biz.auth.model.group.GroupPo;
import com.ksptool.bio.biz.auth.model.group.dto.*;
import com.ksptool.bio.biz.auth.model.group.vo.*;
import com.ksptool.bio.biz.auth.model.permission.PermissionPo;
import com.ksptool.bio.biz.auth.repository.*;
import com.ksptool.bio.biz.core.common.IdsDiff;
import com.ksptool.bio.biz.core.common.SuperEntities;
import com.ksptool.bio.biz.core.common.Switch;
import com.ksptool.bio.biz.core.model.org.OrgPo;
import com.ksptool.bio.biz.core.repository.MenuRepository;
import com.ksptool.bio.biz.core.repository.OrgRepository;
import com.ksptool.bio.commons.dataprocess.Str;

import jakarta.persistence.Tuple;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import static com.ksptool.bio.biz.core.common.TupleMapper.tupleAs;
import static com.ksptool.entities.Entities.as;
import static com.ksptool.entities.Entities.assign;


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

    @Autowired
    private MenuRepository menuRepository;

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
     * 添加用户组
     *
     * @param dto 添加用户组参数
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

        //处理GP关系
        var pPos = pRepository.findAllById(dto.getPermissionIds());
        var gpPos = pPos.stream().map(p -> {
            return new GroupPermissionPo(gId, p.getId());
        }).toList();

        if (!gpPos.isEmpty()) {
            gpRepository.saveAll(gpPos);
        }

        //处理GM关系
        var mPos = menuRepository.findAllById(dto.getMenuIds());
        var gmPos = mPos.stream().map(m -> {
            return new GroupMenuPo(gId, m.getId());
        }).toList();
        
        if (!gmPos.isEmpty()) {
            gmRepository.saveAll(gmPos);
        }

        //处理GD关系 RS=60(指定组织)时才需要处理 如果RS不是60 则直接清空GD关系
        if (dto.getRowScope() == 60) {

            var dPos = orgRepository.findAllById(dto.getDeptIds());
            var gdPos = dPos.stream().map(d -> {
                return new GroupDeptPo(gId, d.getId());
            }).toList();

            //这些所有的组织都必须属于同一个租户
            if(!dPos.isEmpty()){

                var hSet = new HashSet<Long>();
                hSet.addAll(dPos.stream().map(d -> d.getRootId()).toList());

                if(hSet.size() > 1){
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
        if(g.isSystem()){

            //内置用户组不可调整RS数据权限
            if(dto.getRowScope() != null && dto.getRowScope() != g.getRowScope()){
                throw new BizException("内置用户组不允许调整RS数据权限！");
            }

            //内置用户组不可调整状态
            if(dto.getStatus() != null && dto.getStatus() != g.getStatus()){
                throw new BizException("内置用户组不允许调整状态！");
            }

            //内置用户组不可调整编码
            if(dto.getCode() != null && !dto.getCode().equals(g.getCode())){
                throw new BizException("内置用户组不允许调整编码！");
            }

            //获取超级操作权限
            var sa = pRepository.getByCode(SuperEntities.PERMISSION.getCode());

            if(sa == null){
                throw new BizException("系统异常，未能获取超级操作权限！");
            }

            //检测dto里面是否把超级操作权限去除了 以避免用户解除超级组的SA权限导致超级组报废
            if(!dto.getPermissionIds().contains(sa.getId())){
                throw new BizException("内置用户组不允许解除超级操作权限！");
            }

        }

        if (repository.countByCodeExcludeId(dto.getCode(), g.getId()) > 0) {
            throw new BizException("用户组编码 [" + dto.getCode() + "] 已被其他用户组占用,请更换编码后重试!");
        }

        //合并同类项
        assign(dto, g);

        //对比GP + GM + GD关系的差异
        var gpIdsDiff = new IdsDiff(gpRepository.getPidsByGid(g.getId()), dto.getPermissionIds());
        var gmIdsDiff = new IdsDiff(gmRepository.getMidsByGid(g.getId()), dto.getMenuIds());
        var gdIdsDiff = new IdsDiff(gdRepository.getDidsByGid(g.getId()), dto.getDeptIds());

        //处理GP的新增/删除关系
        if(gpIdsDiff.hasAdd()){
            var gpPos = gpIdsDiff.getAddIds().stream().map(id -> new GroupPermissionPo(g.getId(), id)).toList();
            gpRepository.saveAll(gpPos);
        }
        
        if(gpIdsDiff.hasRemove()){
            gpRepository.removeByGidAndPids(g.getId(), gpIdsDiff.getRemoveIds());
        }

        //处理GM的新增/删除关系
        if(gmIdsDiff.hasAdd()){
            var gmPos = gmIdsDiff.getAddIds().stream().map(id -> new GroupMenuPo(g.getId(), id)).toList();
            gmRepository.saveAll(gmPos);
        }
        
        if(gmIdsDiff.hasRemove()){
            gmRepository.removeByGidAndMids(g.getId(), gmIdsDiff.getRemoveIds());
        }

        //处理GD的新增/删除关系 只有RS=60(指定组织)时才需要处理 如果RS不是60 则直接清空GD关系
        if(g.getRowScope() != 60){
            gdRepository.removeByGid(g.getId());
        }

        if(g.getRowScope() == 60){

            if(gdIdsDiff.hasAdd()){
                
                var dPos = orgRepository.findAllById(gdIdsDiff.getAddIds());
                var gdPos = dPos.stream().map(d -> new GroupDeptPo(g.getId(), d.getId())).toList();

                var hSet = new HashSet<Long>();
                hSet.addAll(dPos.stream().map(d -> d.getRootId()).toList());

                //还要把这个G之前的GD也拉出来以防止之前的GD的RootId和新的GD的RootId不一致
                var oldDPos = orgRepository.findAllById(gdRepository.getDeptIdsByGroupId(g.getId()));
                hSet.addAll(oldDPos.stream().map(d -> d.getRootId()).toList());

                if(hSet.size() > 1){
                    throw new BizException("选择的多个组织机构不属于同一个租户!");
                }

                gdRepository.saveAll(gdPos);
            }
            
            if(gdIdsDiff.hasRemove()){
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

        //获取系统中的全部权限列表
        List<PermissionPo> allPermPos = pRepository.findAll();

        //获取该用户组拥有的权限IDS
        var groupPermIds = gpRepository.getPermissionIdsByGroupId(id);

        GetGroupDetailsVo vo = as(po, GetGroupDetailsVo.class);
        List<GroupPermissionDefinitionVo> defVos = new ArrayList<>();

        for (var permission : allPermPos) {

            var defVo = as(permission, GroupPermissionDefinitionVo.class);
            defVo.setHas(1);

            //如果该用户组拥有该权限 则设置为0
            if (groupPermIds.contains(permission.getId())) {
                defVo.setHas(0);
            }

            defVos.add(defVo);
        }

        vo.setPermissions(defVos);

        //如果数据权限为 60(指定组织)时，则需要获取组织列表
        if (po.getRowScope() == 60) {
            var deptIds = gdRepository.getDeptIdsByGroupId(id);
            vo.setDeptIds(deptIds);
        }

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
     * 获取用户组权限菜单视图
     *
     * @param dto 获取用户组权限菜单视图参数
     * @return 用户组权限菜单视图列表
     * @throws BizException 用户组不存在
     */
    public List<GetGroupPermissionMenuViewVo> getGroupPermissionMenuView(GetGroupPermissionMenuViewDto dto) throws BizException {

        GroupPo group = repository.findById(dto.getGroupId()).orElseThrow(() -> new BizException("用户组不存在"));

        //查找菜单列表
        var menuPos = menuRepository.getMenusByKeyword(dto.getKeyword());

        List<GetGroupPermissionMenuViewVo> flatVos = new ArrayList<>();

        //将menuPos转换为flatVos
        for (var po : menuPos) {
            GetGroupPermissionMenuViewVo vo = as(po, GetGroupPermissionMenuViewVo.class);
            vo.setChildren(new ArrayList<>());
            vo.setParentId(null);
            if (po.getParentId() != null) {
                vo.setParentId(po.getParentId());
            }
            flatVos.add(vo);
        }

        //将平面vo转换为tree
        List<GetGroupPermissionMenuViewVo> treeVos = new ArrayList<>();
        Map<Long, GetGroupPermissionMenuViewVo> map = new HashMap<>();

        for (GetGroupPermissionMenuViewVo vo : flatVos) {
            map.put(vo.getId(), vo);
        }

        for (GetGroupPermissionMenuViewVo vo : flatVos) {
            if (vo.getParentId() == null) {
                treeVos.add(vo);
                continue;
            }

            GetGroupPermissionMenuViewVo parent = map.get(vo.getParentId());
            if (parent != null) {
                parent.getChildren().add(vo);
            } else {
                treeVos.add(vo);
            }
        }

        //搜集菜单中的权限列表
        var permissions = new HashSet<String>();
        for (var menuPo : menuPos) {
            permissions.addAll(menuPo.getPermissionCode());
        }

        //查找数据库中不存在的权限
        Set<String> existingPermissions = pRepository.getExistingPermissionsByCode(permissions);
        Set<String> missingPermissions = new HashSet<>(permissions);
        missingPermissions.removeAll(existingPermissions);

        // 设置缺失权限标记
        for (GetGroupPermissionMenuViewVo vo : flatVos) {

            if (StringUtils.isBlank(vo.getPermissionCode())) {
                vo.setMissingPermission(0);
                continue;
            }

            List<String> perms = Str.safeSplit(vo.getPermissionCode(), ";");
            int missingCount = 0;
            int totalCount = perms.size();

            for (String perm : perms) {
                if (missingPermissions.contains(perm)) {
                    missingCount++;
                }
            }

            //如果菜单没有缺失权限，则设置为0
            if (missingCount == 0) {
                vo.setMissingPermission(0);
                continue;
            }

            //如果菜单完全缺失权限，则设置为1
            if (missingCount == totalCount) {
                vo.setMissingPermission(1);
                continue;
            }

            //如果菜单部分缺失权限，则设置为2
            vo.setMissingPermission(2);
        }

        //获取该组拥有的权限
        var groupPerms = pRepository.getPermissionsByGroupId(group.getId());

        //设置菜单当前组是否有权限
        for (var vo : flatVos) {

            var menuPerms = vo.getPermissions();
            var total = menuPerms.size();
            var has = 0;

            for (var menuPerm : menuPerms) {
                for (var groupPerm : groupPerms) {
                    if (menuPerm.equals(groupPerm.getCode())) {
                        has++;
                    }
                }
            }

            vo.setHasPermission(0);

            //如果菜单没有权限，则设置为0
            if (total == 0) {
                vo.setHasPermission(0);
                continue;
            }

            //如果菜单有权限，则设置为1
            if (has >= total) {
                vo.setHasPermission(1);
                continue;
            }

            //如果菜单部分有权限，则设置为2
            if (has > 0) {
                vo.setHasPermission(2);
            }
        }

        //如果hasPermission不为空，则递归过滤
        if (dto.getHasPermission() != null) {
            treeVos = filterMenuTreeByHasPermission(treeVos, dto.getHasPermission());
        }

        return treeVos;
    }

    /**
     * 递归过滤菜单树，只保留hasPermission匹配的节点
     * 如果父节点不符合条件但子节点符合，子节点会被保留
     */
    private List<GetGroupPermissionMenuViewVo> filterMenuTreeByHasPermission(List<GetGroupPermissionMenuViewVo> treeVos, Integer hasPermission) {
        if (treeVos == null || treeVos.isEmpty()) {
            return new ArrayList<>();
        }

        List<GetGroupPermissionMenuViewVo> filtered = new ArrayList<>();

        for (GetGroupPermissionMenuViewVo vo : treeVos) {
            // 递归过滤子节点
            List<GetGroupPermissionMenuViewVo> filteredChildren = filterMenuTreeByHasPermission(vo.getChildren(), hasPermission);

            // 如果当前节点符合条件
            if (vo.getHasPermission() != null && vo.getHasPermission().equals(hasPermission)) {
                vo.setChildren(filteredChildren);
                filtered.add(vo);
                continue;
            }

            // 如果当前节点不符合条件，但子节点符合，保留子节点
            if (!filteredChildren.isEmpty()) {
                filtered.addAll(filteredChildren);
            }
        }

        return filtered;
    }

    /**
     * 获取组权限节点视图
     *
     * @param dto 获取组权限节点视图参数
     * @return 组权限节点视图列表
     * @throws BizException 业务异常
     */
    public PageResult<GetGroupPermissionNodeVo> getGroupPermissionNodeView(GetGroupPermissionNodeDto dto) throws BizException {

        GroupPo group = repository.findById(dto.getGroupId()).orElseThrow(() -> new BizException("用户组不存在"));

        //查找权限节点
        var pPos = pRepository.getPermissionsByKeywordAndGroup(dto.getKeyword(), group.getId(), dto.getHasPermission(), dto.pageRequest());
        List<GetGroupPermissionNodeVo> vos = as(pPos.getContent(), GetGroupPermissionNodeVo.class);

        var groupPerms = pRepository.getPermissionsByGroupId(group.getId());

        for (var vo : vos) {
            vo.setHasPermission(0);
            for (var groupPerm : groupPerms) {
                if (vo.getCode().equals(groupPerm.getCode())) {
                    vo.setHasPermission(1);
                    break;
                }
            }
        }

        return PageResult.success(vos, pPos.getTotalElements());
    }

    /**
     * 授权和取消授权
     *
     * @param dto 授权和取消授权参数
     * @throws BizException 用户组不存在
     */
    @Transactional(rollbackFor = Exception.class)
    public void grantAndRevoke(GrantAndRevokeDto dto) throws BizException {

        GroupPo group = repository.findById(dto.getGroupId()).orElseThrow(() -> new BizException("用户组不存在"));

        //获取当前组拥有的权限
        var groupPerms = pRepository.getPermissionsByGroupId(group.getId());

        //获取要操作的权限
        var permPos = pRepository.getPermissionsByCodes(dto.getPermissionCodes());

        //清空该组下挂载的权限关系
        gpRepository.clearPermissionByGroupId(group.getId());

        //最终的GP权限关系
        var gpPos = new ArrayList<GroupPermissionPo>();

        //模式授权 0:授权 1:取消授权
        if (dto.getType() == 0) {

            //授权 = 当前组拥有的权限 + 要操作的权限
            var mergePerms = new HashSet<>(groupPerms);
            mergePerms.addAll(permPos);

            for (var perm : mergePerms) {
                var gpPo = new GroupPermissionPo();
                gpPo.setGroupId(group.getId());
                gpPo.setPermissionId(perm.getId());
                gpPos.add(gpPo);
            }

        }

        //取消授权
        if (dto.getType() == 1) {

            //取消授权 = 当前组拥有的权限 - 要操作的权限
            var mergePerms = new HashSet<PermissionPo>(groupPerms);

            for (var perm : permPos) {
                mergePerms.remove(perm);
            }

            for (var perm : mergePerms) {
                var gpPo = new GroupPermissionPo();
                gpPo.setGroupId(group.getId());
                gpPo.setPermissionId(perm.getId());
                gpPos.add(gpPo);
            }

        }

        //保存更改
        if (!gpPos.isEmpty()) {
            gpRepository.saveAll(gpPos);
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

        int rsLevel = dto.getRsLevel();

        //校验RS等级 不支持60(指定组织,依赖组配置无法单点模拟)
        if (rsLevel == 60) {
            throw new BizException("模拟器不支持 RS=60(指定组织)");
        }

        //校验RS等级合法值
        boolean validLevel = rsLevel == 0 || rsLevel == 10 || rsLevel == 20
                || rsLevel == 30 || rsLevel == 40 || rsLevel == 50 || rsLevel == 100;
        if (!validLevel) {
            throw new BizException("非法的RS等级: " + rsLevel);
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

        //rsLevel=0 全集团 SQL层直接放行租户全量,前端高亮全部节点
        if (rsLevel == 0) {
            vo.setAllMode(true);
            vo.setVisibleOrgIds(new ArrayList<>());
            return vo;
        }

        //rsLevel=100 拒绝所有 orgIds永远不会匹配到任何ID
        if (rsLevel == 100) {
            vo.setVisibleOrgIds(new ArrayList<>());
            return vo;
        }

        //rsLevel=50 仅本人 SQL层用creator_id过滤,组织树层面无命中节点
        if (rsLevel == 50) {
            vo.setVisibleOrgIds(new ArrayList<>());
            return vo;
        }

        var visibleIds = new HashSet<Long>();

        //rsLevel=10 本公司+下级公司 需要虚拟orgId
        if (rsLevel == 10) {
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

        //rsLevel=20 仅本公司(本公司直属部门,排除子公司及其下部门) 需要虚拟orgId
        if (rsLevel == 20) {
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

        //rsLevel=30 本部门+下级部门 需要虚拟deptId
        if (rsLevel == 30) {
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

        //rsLevel=40 仅本部门 需要虚拟deptId
        if (rsLevel == 40) {
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
