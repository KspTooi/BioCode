package com.ksptool.bio.biz.auth.controller;

import com.ksptool.assembly.entity.web.CommonIdDto;
import com.ksptool.assembly.entity.web.PageResult;
import com.ksptool.assembly.entity.web.Result;
import com.ksptool.bio.biz.auth.model.permission.dto.AddPermissionDto;
import com.ksptool.bio.biz.auth.model.permission.dto.EditPermissionDto;
import com.ksptool.bio.biz.auth.model.permission.dto.GetPermissionListDto;
import com.ksptool.bio.biz.auth.model.permission.vo.GetPermissionDefinitionVo;
import com.ksptool.bio.biz.auth.model.permission.vo.GetPermissionDetailsVo;
import com.ksptool.bio.biz.auth.model.permission.vo.GetPermissionListVo;
import com.ksptool.bio.biz.auth.service.PermissionService;
import com.ksptool.bio.biz.core.service.UserService;
import com.ksptool.bio.biz.core.service.MenuService;
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

import java.util.List;

@PrintLog
@RestController
@RequestMapping("/permission")
@Tag(name = "AUTH-权限管理", description = "权限管理")
public class PermissionController {

    @Autowired
    private PermissionService service;

    @Autowired
    private UserService userService;

    @Autowired
    private MenuService menuService;

    @Operation(summary = "获取权限定义列表")
    @PostMapping("getPermissionDefinition")
    public Result<List<GetPermissionDefinitionVo>> getPermissionDefinition() {
        return Result.success(service.getPermissionDefinition());
    }

    @PreAuthorize("@auth.hasCode('auth:permission:view')")
    @Operation(summary = "获取权限列表")
    @PostMapping("getPermissionList")
    public PageResult<GetPermissionListVo> getPermissionList(@RequestBody @Valid GetPermissionListDto dto) {
        return service.getPermissionList(dto);
    }

    @PreAuthorize("@auth.hasCode('auth:permission:view')")
    @Operation(summary = "获取权限详情")
    @PostMapping("getPermissionDetails")
    public Result<GetPermissionDetailsVo> getPermissionDetails(@RequestBody @Valid CommonIdDto dto) throws Exception {
        return Result.success(service.getPermissionDetails(dto.getId()));
    }

    @PreAuthorize("@auth.hasCode('auth:permission:add')")
    @Operation(summary = "新增权限")
    @PostMapping("addPermission")
    public Result<String> addPermission(@RequestBody @Valid AddPermissionDto dto) throws Exception {
        service.addPermission(dto);
        return Result.success("新增成功");
    }

    @PreAuthorize("@auth.hasCode('auth:permission:edit')")
    @Operation(summary = "编辑权限")
    @PostMapping("editPermission")
    public Result<String> editPermission(@RequestBody @Valid EditPermissionDto dto) throws Exception {
        service.editPermission(dto);

        //给拥有该权限的用户加版本
        userService.increaseDvByPermissionId(dto.getId());

        //清菜单缓存
        menuService.clearUserMenuTreeCache();
        return Result.success("修改成功");
    }

    @PreAuthorize("@auth.hasCode('auth:permission:remove')")
    @Operation(summary = "删除权限")
    @PostMapping("removePermission")
    @CacheEvict(cacheNames = {"userSession", "userProfile", "menuTree"}, allEntries = true)
    public Result<String> removePermission(@RequestBody @Valid CommonIdDto dto) throws Exception {
        service.removePermission(dto);
        return Result.success("success");
    }
}
