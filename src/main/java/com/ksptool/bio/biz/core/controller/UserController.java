package com.ksptool.bio.biz.core.controller;

import com.ksptool.assembly.entity.web.CommonIdDto;
import com.ksptool.assembly.entity.web.PageResult;
import com.ksptool.assembly.entity.web.Result;
import com.ksptool.bio.biz.auth.common.aop.RowScope;
import com.ksptool.bio.biz.auth.common.aop.SystemScope;
import com.ksptool.bio.biz.core.model.user.dto.*;
import com.ksptool.bio.biz.core.model.user.vo.GetUserDetailsVo;
import com.ksptool.bio.biz.core.model.user.vo.GetUserListVo;
import com.ksptool.bio.biz.core.service.MenuService;
import com.ksptool.bio.biz.core.service.UserService;
import com.ksptool.bio.commons.annotation.PrintLog;
import com.ksptool.bio.commons.dataprocess.ImportWizard;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;


@PrintLog
@RestController
@RequestMapping("/user")
@Tag(name = "CORE-用户管理", description = "用户管理")
@RowScope(mode = RowScope.Mode.ROOT_ONLY)
@SystemScope
public class UserController {

    @Autowired
    private UserService service;

    @Autowired
    private MenuService menuService;

    @Operation(summary = "获取用户列表")
    @PostMapping("getUserList")
    public PageResult<GetUserListVo> getUserList(@RequestBody @Valid GetUserListDto dto) {
        return service.getUserList(dto);
    }

    @PreAuthorize("@auth.hasCode('core:user:view')")
    @Operation(summary = "获取用户详情")
    @PostMapping("getUserDetails")
    public Result<GetUserDetailsVo> getUserDetails(@RequestBody @Valid CommonIdDto dto) throws Exception {
        return Result.success(service.getUserDetails(dto.getId()));
    }

    @PreAuthorize("@auth.hasCode('core:user:add')")
    @Operation(summary = "新增用户")
    @PrintLog(sensitiveFields = "password")
    @PostMapping("addUser")
    public Result<String> addUser(@RequestBody @Valid AddUserDto dto) throws Exception {
        service.addUser(dto);
        return Result.success("新增成功");
    }

    @PreAuthorize("@auth.hasCode('core:user:edit')")
    @Operation(summary = "编辑用户")
    @PrintLog(sensitiveFields = "password")
    @PostMapping("editUser")
    @CacheEvict(cacheNames = {"userSession", "userProfile", "menuTree"}, allEntries = true)
    public Result<String> editUser(@RequestBody @Valid EditUserDto dto) throws Exception {

        service.editUser(dto);

        //给用户加版本
        service.increaseDv(List.of(dto.getId()));

        //清除用户菜单缓存
        menuService.clearUserMenuTreeCacheByUserId(dto.getId());
        return Result.success("修改成功");
    }


    @PreAuthorize("@auth.hasCode('core:user:remove')")
    @Operation(summary = "删除用户")
    @PostMapping("removeUser")
    @CacheEvict(cacheNames = {"userSession", "userProfile", "menuTree"}, allEntries = true)
    public Result<String> removeUser(@RequestBody @Valid CommonIdDto dto) throws Exception {

        service.removeUser(dto.getId());

        //给用户加版本
        service.increaseDv(List.of(dto.getId()));

        //清除用户菜单缓存
        menuService.clearUserMenuTreeCacheByUserId(dto.getId());
        return Result.success("删除成功");
    }


    @PreAuthorize("@auth.hasCode('core:user:import')")
    @Operation(summary = "导入用户")
    @PostMapping("importUser")
    public Result<String> importUser(@RequestParam("file") MultipartFile file) throws Exception {

        var iw = new ImportWizard<>(file, ImportUserDto.class);
        iw.transfer();

        var errors = iw.validate();

        if (StringUtils.isNotBlank(errors)) {
            return Result.error(errors);
        }

        var data = iw.getData();
        var count = service.importUser(data);
        return Result.success("导入成功,已导入数据:" + count + "条", null);
    }

    @PreAuthorize("@auth.hasCode('core:user:batch_edit')")
    @Operation(summary = "批量编辑用户")
    @PostMapping("batchEditUser")
    @CacheEvict(cacheNames = {"userSession", "userProfile", "menuTree"}, allEntries = true)
    public Result<String> batchEditUser(@RequestBody @Valid BatchEditUserDto dto) throws Exception {

        //校验变更组织架构ID
        if (dto.getKind() == 3 && dto.getOrgId() == null) {
            return Result.error("变更组织架构ID不能为空");
        }

        var count = service.batchEditUser(dto);

        //给用户加版本
        service.increaseDv(dto.getIds());

        //清除用户菜单缓存
        menuService.clearUserMenuTreeCache();
        return Result.success("批量操作成功,已操作数据:" + count + "条");
    }


}
