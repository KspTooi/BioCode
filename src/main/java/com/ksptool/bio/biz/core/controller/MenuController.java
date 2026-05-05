package com.ksptool.bio.biz.core.controller;


import com.ksptool.assembly.entity.exception.BizException;
import com.ksptool.assembly.entity.web.CommonIdDto;
import com.ksptool.assembly.entity.web.Result;
import com.ksptool.bio.biz.auth.service.SessionService;
import com.ksptool.bio.biz.core.model.menu.dto.AddMenuDto;
import com.ksptool.bio.biz.core.model.menu.dto.EditMenuDto;
import com.ksptool.bio.biz.core.model.menu.dto.GetMenuTreeDto;
import com.ksptool.bio.biz.core.model.menu.vo.GetMenuDetailsVo;
import com.ksptool.bio.biz.core.model.menu.vo.GetMenuTreeVo;
import com.ksptool.bio.biz.core.model.menu.vo.GetUserMenuTreeVo;
import com.ksptool.bio.biz.core.service.MenuService;
import com.ksptool.bio.commons.annotation.PrintLog;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static com.ksptool.bio.biz.auth.service.SessionService.session;

@PrintLog
@RestController
@RequestMapping("/menu")
@Tag(name = "CORE-菜单管理", description = "菜单与按钮管理")
@Slf4j
public class MenuController {

    @Autowired
    private MenuService menuService;

    @PostMapping("/getUserMenuTree")
    @Operation(summary = "获取用户菜单与按钮树(这是显示到用户左侧菜单的数据,这个接口带有缓存)")
    public Result<List<GetUserMenuTreeVo>> getUserMenuTree() throws Exception {
        return Result.success(menuService.getUserMenuTree(session().getUserId()));
    }


    @PostMapping("/getMenuTree")
    @Operation(summary = "获取菜单与按钮树(这是显示到菜单管理界面的数据,这个接口不带有缓存)")
    public Result<List<GetMenuTreeVo>> getMenuTree(@RequestBody @Valid GetMenuTreeDto dto) throws Exception {
        return Result.success(menuService.getMenuTree(dto));
    }

    @PreAuthorize("@auth.hasCode('core:menu:add')")
    @PostMapping("/addMenu")
    @Operation(summary = "新增菜单与按钮")
    @CacheEvict(cacheNames = {"userSession", "userProfile", "menuTree"}, allEntries = true)
    public Result<String> addMenu(@RequestBody @Valid AddMenuDto dto) throws Exception {

        //验证输入参数
        if (dto.validate() != null) {
            return Result.error(dto.validate());
        }

        //检查当前用户是否在超级租户里面
        if (!SessionService.isSuperRoot()) {
            throw new BizException("您所在的租户不是超级租户，无法新增菜单。");
        }

        menuService.addMenu(dto);
        return Result.success("新增成功");
    }

    @PreAuthorize("@auth.hasCode('core:menu:edit')")
    @PostMapping("/editMenu")
    @Operation(summary = "编辑菜单与按钮")
    @CacheEvict(cacheNames = {"userSession", "userProfile", "menuTree"}, allEntries = true)
    public Result<String> editMenu(@RequestBody @Valid EditMenuDto dto) throws Exception {

        //验证输入参数
        if (dto.validate() != null) {
            return Result.error(dto.validate());
        }

        //检查当前用户是否在超级租户里面
        if (!SessionService.isSuperRoot()) {
            throw new BizException("您所在的租户不是超级租户，无法编辑菜单。");
        }

        menuService.editMenu(dto);
        return Result.success("编辑成功");
    }

    @PreAuthorize("@auth.hasCode('core:menu:details')")
    @PostMapping("/getMenuDetails")
    @Operation(summary = "获取菜单与按钮详情")
    public Result<GetMenuDetailsVo> getMenuDetails(@RequestBody @Valid CommonIdDto dto) throws Exception {
        return Result.success(menuService.getMenuDetails(dto));
    }


    @PreAuthorize("@auth.hasCode('core:menu:remove')")
    @PostMapping("/removeMenu")
    @Operation(summary = "删除菜单与按钮")
    @CacheEvict(cacheNames = {"userSession", "userProfile", "menuTree"}, allEntries = true)
    public Result<String> removeMenu(@RequestBody @Valid CommonIdDto dto) throws Exception {
        menuService.removeMenu(dto);
        return Result.success("删除成功");
    }

}
