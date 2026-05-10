package com.ksptool.bio.biz.auth.controller;


import com.ksptool.assembly.entity.exception.BizException;
import com.ksptool.assembly.entity.web.CommonIdDto;
import com.ksptool.assembly.entity.web.PageResult;
import com.ksptool.assembly.entity.web.Result;
import com.ksptool.bio.biz.auth.common.aop.RowScope;
import com.ksptool.bio.biz.auth.model.group.dto.*;
import com.ksptool.bio.biz.auth.model.group.vo.GetGroupDetailsVo;
import com.ksptool.bio.biz.auth.model.group.vo.GetGroupListVo;
import com.ksptool.bio.biz.auth.model.group.vo.SimulateRsVo;
import com.ksptool.bio.biz.auth.service.GroupService;
import com.ksptool.bio.biz.auth.service.SessionService;
import com.ksptool.bio.biz.core.service.MenuService;
import com.ksptool.bio.biz.core.service.UserService;
import com.ksptool.bio.commons.annotation.PrintLog;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@PrintLog
@RestController
@RequestMapping("/group")
@Tag(name = "AUTH-用户组管理", description = "用户组管理")
@RowScope
public class GroupController {

    @Autowired
    private GroupService service;

    @Autowired
    private MenuService menuService;

    @Autowired
    private UserService userService;


    @Operation(summary = "获取用户组列表")
    @PostMapping("getGroupList")
    public PageResult<GetGroupListVo> getGroupList(@RequestBody @Valid GetGroupListDto dto) {
        return service.getGroupList(dto);
    }

    @PreAuthorize("@auth.hasCode('auth:group:add')")
    @Operation(summary = "新增用户组")
    @PostMapping("addGroup")
    public Result<String> addGroup(@RequestBody @Valid AddGroupDto dto) throws Exception {
        service.addGroup(dto);
        return Result.success("新增成功");
    }

    @PreAuthorize("@auth.hasCode('auth:group:edit')")
    @Operation(summary = "编辑用户组")
    @PostMapping("editGroup")
    @CacheEvict(cacheNames = {"userSession", "userProfile", "menuTree"}, allEntries = true)
    public Result<String> editGroup(@RequestBody @Valid EditGroupDto dto) throws Exception {

        service.editGroup(dto);

        //给拥有该组的用户加版本
        userService.increaseDvByGroupId(dto.getId());

        //清菜单缓存
        menuService.clearUserMenuTreeCache();
        return Result.success("修改成功");
    }

    @PreAuthorize("@auth.hasCode('auth:group:details')")
    @Operation(summary = "获取用户组详情")
    @PostMapping("getGroupDetails")
    public Result<GetGroupDetailsVo> getGroupDetails(@RequestBody @Valid CommonIdDto dto) throws Exception {
        return Result.success(service.getGroupDetails(dto.getId()));
    }

    @PreAuthorize("@auth.hasCode('auth:group:remove')")
    @Operation(summary = "移除用户组")
    @PostMapping("removeGroup")
    @CacheEvict(cacheNames = {"userSession", "userProfile", "menuTree"}, allEntries = true)
    public Result<String> removeGroup(@RequestBody @Valid CommonIdDto dto) throws Exception {
        service.removeGroup(dto);
        menuService.clearUserMenuTreeCache();

        //给拥有该组的用户加版本
        userService.increaseDvByGroupId(dto.getId());

        //清菜单缓存
        menuService.clearUserMenuTreeCache();
        return Result.success("删除成功");
    }

    @Operation(summary = "更新组权限(GP)")
    @PostMapping("updateGroupGp")
    @CacheEvict(cacheNames = {"userSession", "userProfile", "menuTree"}, allEntries = true)
    public Result<String> updateGroupGp(@RequestBody @Valid UpdateGroupGpDto dto) throws Exception {

        if(!SessionService.hasSuperCode()){
            throw new BizException("只有超级管理员才能更新组权限(GP)");
        }

        service.updateGroupGp(dto);

        //给拥有该组的用户加版本
        userService.increaseDvByGroupId(dto.getGroupId());

        //清菜单缓存
        menuService.clearUserMenuTreeCache();
        return Result.success("更新组权限(GP)成功");
    }


    @PreAuthorize("@auth.hasCode('auth:group:edit')")
    @Operation(summary = "更新组菜单(GM)")
    @PostMapping("updateGroupGm")
    @CacheEvict(cacheNames = {"userSession", "userProfile", "menuTree"}, allEntries = true)
    public Result<String> updateGroupGm(@RequestBody @Valid UpdateGroupGmDto dto) throws Exception {
        service.updateGroupGm(dto);

        //给拥有该组的用户加版本
        userService.increaseDvByGroupId(dto.getGroupId());

        //清菜单缓存
        menuService.clearUserMenuTreeCache();
        return Result.success("更新组菜单(GM)成功");
    }

    @PreAuthorize("@auth.hasCode('auth:group:view')")
    @Operation(summary = "模拟RS数据权限")
    @PostMapping("simulateRs")
    public Result<SimulateRsVo> simulateRs(@RequestBody @Valid SimulateRsDto dto) throws Exception {
        return Result.success(service.simulateRs(dto));
    }


}
