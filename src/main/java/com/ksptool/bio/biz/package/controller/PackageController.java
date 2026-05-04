package com.ksptool.bio.biz.package.controller;

import com.ksptool.assembly.entity.web.CommonIdDto;
import com.ksptool.assembly.entity.web.PageResult;
import com.ksptool.assembly.entity.web.Result;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import com.ksptool.bio.commons.annotation.PrintLog;

import com.ksptool.bio.biz.package.service.PackageService;
import com.ksptool.bio.biz.package.model.dto.AddPackageDto;
import com.ksptool.bio.biz.package.model.dto.EditPackageDto;
import com.ksptool.bio.biz.package.model.dto.GetPackageListDto;
import com.ksptool.bio.biz.package.model.vo.GetPackageListVo;
import com.ksptool.bio.biz.package.model.vo.GetPackageDetailsVo;

@PrintLog
@RestController
@RequestMapping("/package")
@Tag(name = "菜单包", description = "菜单包")
@Slf4j
public class PackageController {

    @Autowired
    private PackageService packageService;

    @PreAuthorize("@auth.hasCode('core:package:view')")
    @PostMapping("/getPackageList")
    @Operation(summary ="查询菜单包列表")
    public PageResult<GetPackageListVo> getPackageList(@RequestBody @Valid GetPackageListDto dto) throws Exception{
        return packageService.getPackageList(dto);
    }

    @PreAuthorize("@auth.hasCode('core:package:add')")
    @Operation(summary ="新增菜单包")
    @PostMapping("/addPackage")
    public Result<String> addPackage(@RequestBody @Valid AddPackageDto dto) throws Exception{
		packageService.addPackage(dto);
        return Result.success("新增成功");
    }

    @PreAuthorize("@auth.hasCode('core:package:edit')")
    @Operation(summary ="编辑菜单包")
    @PostMapping("/editPackage")
    public Result<String> editPackage(@RequestBody @Valid EditPackageDto dto) throws Exception{
		packageService.editPackage(dto);
        return Result.success("修改成功");
    }

    @PreAuthorize("@auth.hasCode('core:package:view')")
    @Operation(summary ="查询菜单包详情")
    @PostMapping("/getPackageDetails")
    public Result<GetPackageDetailsVo> getPackageDetails(@RequestBody @Valid CommonIdDto dto) throws Exception{
        GetPackageDetailsVo details = packageService.getPackageDetails(dto);
        if(details == null){
            return Result.error("无数据");
        }
        return Result.success(details);
    }

    @PreAuthorize("@auth.hasCode('core:package:remove')")
    @Operation(summary ="删除菜单包")
    @PostMapping("/removePackage")
    public Result<String> removePackage(@RequestBody @Valid CommonIdDto dto) throws Exception{
        packageService.removePackage(dto);
        return Result.success("操作成功");
    }

}
