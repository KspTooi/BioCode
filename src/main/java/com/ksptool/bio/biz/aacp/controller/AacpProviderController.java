package com.ksptool.bio.biz.aacp.controller;

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

import com.ksptool.bio.biz.aacp.service.ProviderService;
import com.ksptool.bio.biz.aacp.model.provider.dto.AddProviderDto;
import com.ksptool.bio.biz.aacp.model.provider.dto.EditProviderDto;
import com.ksptool.bio.biz.aacp.model.provider.dto.GetProviderListDto;
import com.ksptool.bio.biz.aacp.model.provider.vo.GetProviderListVo;
import com.ksptool.bio.biz.aacp.model.provider.vo.GetProviderDetailsVo;

@PrintLog
@RestController
@RequestMapping("/provider")
@Tag(name = "模型供应商", description = "模型供应商")
@Slf4j
public class ProviderController {

    @Autowired
    private ProviderService providerService;

    @PreAuthorize("@auth.hasCode('aacp:provider:view')")
    @PostMapping("/getProviderList")
    @Operation(summary ="查询模型供应商列表")
    public PageResult<GetProviderListVo> getProviderList(@RequestBody @Valid GetProviderListDto dto) throws Exception{
        return providerService.getProviderList(dto);
    }

    @PreAuthorize("@auth.hasCode('aacp:provider:add')")
    @Operation(summary ="新增模型供应商")
    @PostMapping("/addProvider")
    public Result<String> addProvider(@RequestBody @Valid AddProviderDto dto) throws Exception{
		providerService.addProvider(dto);
        return Result.success("新增成功");
    }

    @PreAuthorize("@auth.hasCode('aacp:provider:edit')")
    @Operation(summary ="编辑模型供应商")
    @PostMapping("/editProvider")
    public Result<String> editProvider(@RequestBody @Valid EditProviderDto dto) throws Exception{
		providerService.editProvider(dto);
        return Result.success("修改成功");
    }

    @PreAuthorize("@auth.hasCode('aacp:provider:view')")
    @Operation(summary ="查询模型供应商详情")
    @PostMapping("/getProviderDetails")
    public Result<GetProviderDetailsVo> getProviderDetails(@RequestBody @Valid CommonIdDto dto) throws Exception{
        GetProviderDetailsVo details = providerService.getProviderDetails(dto);
        if(details == null){
            return Result.error("无数据");
        }
        return Result.success(details);
    }

    @PreAuthorize("@auth.hasCode('aacp:provider:remove')")
    @Operation(summary ="删除模型供应商")
    @PostMapping("/removeProvider")
    public Result<String> removeProvider(@RequestBody @Valid CommonIdDto dto) throws Exception{
        providerService.removeProvider(dto);
        return Result.success("操作成功");
    }

}
