package com.ksptool.bio.biz.core.service;

import com.ksptool.assembly.entity.exception.AuthException;
import com.ksptool.assembly.entity.exception.BizException;
import com.ksptool.assembly.entity.web.CommonIdDto;
import com.ksptool.bio.biz.auth.repository.PermissionRepository;
import com.ksptool.bio.biz.auth.service.SessionService;
import com.ksptool.bio.biz.core.common.TreeBuilder;
import com.ksptool.bio.biz.core.model.menu.MenuPo;
import com.ksptool.bio.biz.core.model.menu.dto.AddMenuDto;
import com.ksptool.bio.biz.core.model.menu.dto.EditMenuDto;
import com.ksptool.bio.biz.core.model.menu.dto.GetMenuTreeDto;
import com.ksptool.bio.biz.core.model.menu.vo.GetMenuDetailsVo;
import com.ksptool.bio.biz.core.model.menu.vo.GetMenuTreeVo;
import com.ksptool.bio.biz.core.model.menu.vo.GetUserMenuTreeVo;
import com.ksptool.bio.biz.core.repository.MenuPackRepository;
import com.ksptool.bio.biz.core.repository.MenuRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import static com.ksptool.entities.Entities.as;
import static com.ksptool.entities.Entities.assign;

/**
 * @author KspTooi
 * @since 1.2.8(H).1
 */
@Service
public class MenuService {

    @Autowired
    private PermissionRepository permissionRepository;

    @Autowired
    private MenuPackRepository menuPackRepository;

    @Autowired
    private MenuRepository menuRepository;

    /**
     * 获取用户菜单与按钮树(该函数带有缓存)
     *
     * @param uid 用户ID 用于缓存键
     * @return 用户菜单与按钮树
     * @throws BizException 业务异常
     */
    @Cacheable(cacheNames = "menuTree", key = "'userMenuTree:' + #uid")
    public List<GetUserMenuTreeVo> getUserMenuTree(Long uid) throws BizException, AuthException {

        var allMenuPos = menuRepository.getUserMenuTree();
        var flatVos = new ArrayList<GetUserMenuTreeVo>();

        var authorities = SessionService.authorities();

        //将list转换为平面vo
        for (MenuPo po : allMenuPos) {

            //在此过滤当前用户无权限访问的菜单
            if (!po.hasPermission(authorities)) {
                continue;
            }

            var vo = as(po, GetUserMenuTreeVo.class);
            vo.setChildren(new ArrayList<>());
            vo.setParentId(null);
            if (po.getParentId() != null) {
                vo.setParentId(po.getParentId());
            }
            flatVos.add(vo);
        }

        List<GetUserMenuTreeVo> treeVos = TreeBuilder.build(flatVos);
        pruneEmptyDirectories(treeVos);
        return treeVos;
    }


    /**
     * 获取菜单与按钮树
     *
     * @param dto 获取菜单与按钮树参数
     * @return 菜单与按钮树
     * @throws BizException 业务异常
     */
    public List<GetMenuTreeVo> getMenuTree(GetMenuTreeDto dto) throws BizException {

        List<MenuPo> list = menuRepository.getMenuTree(dto);

        List<GetMenuTreeVo> flatVos = new ArrayList<>();

        //将list转换为平面vo
        for (MenuPo po : list) {
            GetMenuTreeVo vo = as(po, GetMenuTreeVo.class);
            vo.setChildren(new ArrayList<>());
            vo.setParentId(null);
            if (po.getParentId() != null) {
                vo.setParentId(po.getParentId());
            }

            flatVos.add(vo);
        }

        return TreeBuilder.build(flatVos);
    }

    /**
     * 新增菜单与按钮(这个接口会全量清除所有用户菜单缓存)
     *
     * @param dto 新增菜单与按钮参数
     * @throws BizException 业务异常
     */
    @CacheEvict(cacheNames = "menuTree", allEntries = true)
    @Transactional(rollbackFor = Exception.class)
    public void addMenu(AddMenuDto dto) throws BizException {

        //构建菜单PO
        MenuPo menuPo = as(dto, MenuPo.class);

        //如果父级id不为空，查询父级菜单
        if (dto.getParentId() != null) {

            MenuPo parent = menuRepository.findById(dto.getParentId()).orElseThrow(() -> new BizException("新增失败,父级菜单不存在."));

            //校验父级菜单可达性 0:目录 1:菜单 2:按钮
            if (dto.getKind() == 0) {
                //目录只能放置于目录之下
                if (parent.getKind() != 0) {
                    throw new BizException("新增失败,目录只能放置于目录之下.");
                }
            }

            if (dto.getKind() == 1) {
                //菜单只能放置于目录之下
                if (parent.getKind() != 0) {
                    throw new BizException("新增失败,菜单只能放置于目录之下.");
                }
            }

            if (dto.getKind() == 2) {
                //按钮只能放置于菜单之下
                if (parent.getKind() != 1) {
                    throw new BizException("新增失败,按钮只能放置于菜单之下.");
                }
            }

            menuPo.setParentId(parent.getId());
        }

        menuRepository.save(menuPo);
    }

    /**
     * 编辑菜单与按钮(这个接口会全量清除所有用户菜单缓存)
     *
     * @param dto 编辑菜单与按钮参数
     * @throws BizException 业务异常
     */
    @CacheEvict(cacheNames = "menuTree", allEntries = true)
    @Transactional(rollbackFor = Exception.class)
    public void editMenu(EditMenuDto dto) throws BizException {

        MenuPo menuPo = menuRepository.findById(dto.getId()).orElseThrow(() -> new BizException("编辑失败,数据不存在或无权限访问."));
        assign(dto, menuPo);

        //如果父级id不为空，查询父级资源
        if (dto.getParentId() != null) {
            MenuPo parent = menuRepository.findById(dto.getParentId()).orElseThrow(() -> new BizException("新增失败,父级菜单不存在."));

            //校验父级菜单可达性 0:目录 1:菜单 2:按钮
            if (dto.getKind() == 0) {
                //目录只能放置于目录之下
                if (parent.getKind() != 0) {
                    throw new BizException("编辑失败,目录只能放置于目录之下.");
                }
            }

            if (dto.getKind() == 1) {
                //菜单只能放置于目录之下
                if (parent.getKind() != 0) {
                    throw new BizException("编辑失败,菜单只能放置于目录之下.");
                }
            }

            if (dto.getKind() == 2) {
                //按钮只能放置于菜单之下
                if (parent.getKind() != 1) {
                    throw new BizException("编辑失败,按钮只能放置于菜单之下.");
                }
            }

            //校验父级资源不能是自身
            if (parent.getId().equals(menuPo.getId())) {
                throw new BizException("编辑失败,父级资源不能是自身.");
            }

            menuPo.setParentId(parent.getId());
        }

        menuRepository.save(menuPo);
    }

    /**
     * 获取菜单与按钮详情
     *
     * @param dto 获取菜单与按钮详情参数
     * @return 菜单与按钮详情
     * @throws BizException 业务异常
     */
    public GetMenuDetailsVo getMenuDetails(CommonIdDto dto) throws BizException {

        MenuPo menuPo = menuRepository.findById(dto.getId()).orElseThrow(() -> new BizException("获取详情失败,数据不存在或无权限访问."));
        GetMenuDetailsVo vo = as(menuPo, GetMenuDetailsVo.class);
        if (menuPo.getParentId() != null) {
            vo.setParentId(menuPo.getParentId());
        }
        return vo;
    }

    /**
     * 删除菜单与按钮(这个接口会全量清除所有用户菜单缓存)
     *
     * @param dto 删除菜单与按钮参数
     * @throws BizException 业务异常
     */
    @CacheEvict(cacheNames = "menuTree", allEntries = true)
    @Transactional(rollbackFor = Exception.class)
    public void removeMenu(CommonIdDto dto) throws BizException {

        //删除单个菜单与按钮
        if (!dto.isBatch()) {
            MenuPo po = menuRepository.findById(dto.getId()).orElseThrow(() -> new BizException("删除失败,数据不存在."));

            if (menuRepository.getMenuChildrenCount(po.getId()) > 0) {
                throw new BizException("无法移除: " + po.getName() + " ,请先移除子项.");
            }

            menuRepository.deleteById(po.getId());
            menuPackRepository.removeByMid(po.getId());
            return;
        }

        //如果删除多个菜单与按钮，则需要判断是否有子资源
        for (Long id : dto.getIds()) {
            MenuPo po = menuRepository.findById(id).orElseThrow(() -> new BizException("删除失败,数据不存在."));

            if (menuRepository.getMenuChildrenCount(id) > 0) {
                throw new BizException("无法移除: " + po.getName() + " ,请先移除子项.");
            }

            menuRepository.deleteById(id);
            menuPackRepository.removeByMid(id);
        }

    }


    /**
     * 清除用户菜单缓存
     *
     * @param uid 用户ID 用于缓存键
     */
    @CacheEvict(cacheNames = "menuTree", key = "'userMenuTree:' + #uid")
    public void clearUserMenuTreeCacheByUserId(Long uid) {

    }

    /**
     * 清除全部用户菜单缓存
     *
     */
    @CacheEvict(cacheNames = "menuTree", allEntries = true)
    public void clearUserMenuTreeCache() {

    }

    /**
     * 递归修剪空目录 (自底向上)
     *
     * @param treeNodes 树形节点列表
     */
    private void pruneEmptyDirectories(List<GetUserMenuTreeVo> treeNodes) {
        if (treeNodes == null || treeNodes.isEmpty()) {
            return;
        }

        // 使用 Iterator 进行安全的遍历和删除
        Iterator<GetUserMenuTreeVo> iterator = treeNodes.iterator();
        while (iterator.hasNext()) {
            GetUserMenuTreeVo node = iterator.next();

            //优先向下递归，处理该节点的所有子节点（保证自底向上）
            if (node.getChildren() != null && !node.getChildren().isEmpty()) {
                pruneEmptyDirectories(node.getChildren());
            }

            //子节点处理完毕后，判断当前节点本身是否变成了“空壳目录”
            //假设你们的 VO 中有一个方法判断是否为目录，例如 getType() == 0 或者 isDirectory()
            //这里以 type == 0 代表目录为例 (请替换为你实际的判断逻辑)
            boolean isDirectory = node.getKind() != null && node.getKind() == 0;

            boolean hasNoChildren = node.getChildren() == null || node.getChildren().isEmpty();

            //如果当前节点是目录，并且没有子节点了，直接砍掉
            if (isDirectory && hasNoChildren) {
                iterator.remove();
            }
        }
    }

}
