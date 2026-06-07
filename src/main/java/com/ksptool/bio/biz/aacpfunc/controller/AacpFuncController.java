package com.ksptool.bio.biz.aacpfunc.controller;

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

import com.ksptool.bio.biz.aacpfunc.service.AacpFuncService;
import com.ksptool.bio.biz.aacpfunc.model.dto.AddAacpFuncDto;
import com.ksptool.bio.biz.aacpfunc.model.dto.EditAacpFuncDto;
import com.ksptool.bio.biz.aacpfunc.model.dto.GetAacpFuncListDto;
import com.ksptool.bio.biz.aacpfunc.model.vo.GetAacpFuncListVo;
import com.ksptool.bio.biz.aacpfunc.model.vo.GetAacpFuncDetailsVo;

@PrintLog
@RestController
@RequestMapping("/aacpFunc")
@Tag(name = "微函数", description = "微函数")
@Slf4j
public class AacpFuncController {

    @Autowired
    private AacpFuncService aacpFuncService;

    @PreAuthorize("@auth.hasCode('accp:func:view')")
    @PostMapping("/getAacpFuncList")
    @Operation(summary ="查询微函数列表")
    public PageResult<GetAacpFuncListVo> getAacpFuncList(@RequestBody @Valid GetAacpFuncListDto dto) throws Exception{
        return aacpFuncService.getAacpFuncList(dto);
    }

    @PreAuthorize("@auth.hasCode('accp:func:add')")
    @Operation(summary ="新增微函数")
    @PostMapping("/addAacpFunc")
    public Result<String> addAacpFunc(@RequestBody @Valid AddAacpFuncDto dto) throws Exception{
		aacpFuncService.addAacpFunc(dto);
        return Result.success("新增成功");
    }

    @PreAuthorize("@auth.hasCode('accp:func:edit')")
    @Operation(summary ="编辑微函数")
    @PostMapping("/editAacpFunc")
    public Result<String> editAacpFunc(@RequestBody @Valid EditAacpFuncDto dto) throws Exception{
		aacpFuncService.editAacpFunc(dto);
        return Result.success("修改成功");
    }

    @PreAuthorize("@auth.hasCode('accp:func:view')")
    @Operation(summary ="查询微函数详情")
    @PostMapping("/getAacpFuncDetails")
    public Result<GetAacpFuncDetailsVo> getAacpFuncDetails(@RequestBody @Valid CommonIdDto dto) throws Exception{
        GetAacpFuncDetailsVo details = aacpFuncService.getAacpFuncDetails(dto);
        if(details == null){
            return Result.error("无数据");
        }
        return Result.success(details);
    }

    @PreAuthorize("@auth.hasCode('accp:func:remove')")
    @Operation(summary ="删除微函数")
    @PostMapping("/removeAacpFunc")
    public Result<String> removeAacpFunc(@RequestBody @Valid CommonIdDto dto) throws Exception{
        aacpFuncService.removeAacpFunc(dto);
        return Result.success("操作成功");
    }

}
