package com.ksptool.bio.biz.assembly.controller;

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

import com.ksptool.bio.biz.assembly.service.OpRcdService;
import com.ksptool.bio.biz.assembly.model.dto.AddOpRcdDto;
import com.ksptool.bio.biz.assembly.model.dto.EditOpRcdDto;
import com.ksptool.bio.biz.assembly.model.dto.GetOpRcdListDto;
import com.ksptool.bio.biz.assembly.model.vo.GetOpRcdListVo;
import com.ksptool.bio.biz.assembly.model.vo.GetOpRcdDetailsVo;

@PrintLog
@RestController
@RequestMapping("/opRcd")
@Tag(name = "输出方案执行记录", description = "输出方案执行记录")
@Slf4j
public class OpRcdController {

    @Autowired
    private OpRcdService opRcdService;

    @PreAuthorize("@auth.hasCode('assembly:op:rcd:view')")
    @PostMapping("/getOpRcdList")
    @Operation(summary ="查询输出方案执行记录列表")
    public PageResult<GetOpRcdListVo> getOpRcdList(@RequestBody @Valid GetOpRcdListDto dto) throws Exception{
        return opRcdService.getOpRcdList(dto);
    }

    @PreAuthorize("@auth.hasCode('assembly:op:rcd:add')")
    @Operation(summary ="新增输出方案执行记录")
    @PostMapping("/addOpRcd")
    public Result<String> addOpRcd(@RequestBody @Valid AddOpRcdDto dto) throws Exception{
		opRcdService.addOpRcd(dto);
        return Result.success("新增成功");
    }

    @PreAuthorize("@auth.hasCode('assembly:op:rcd:edit')")
    @Operation(summary ="编辑输出方案执行记录")
    @PostMapping("/editOpRcd")
    public Result<String> editOpRcd(@RequestBody @Valid EditOpRcdDto dto) throws Exception{
		opRcdService.editOpRcd(dto);
        return Result.success("修改成功");
    }

    @PreAuthorize("@auth.hasCode('assembly:op:rcd:view')")
    @Operation(summary ="查询输出方案执行记录详情")
    @PostMapping("/getOpRcdDetails")
    public Result<GetOpRcdDetailsVo> getOpRcdDetails(@RequestBody @Valid CommonIdDto dto) throws Exception{
        GetOpRcdDetailsVo details = opRcdService.getOpRcdDetails(dto);
        if(details == null){
            return Result.error("无数据");
        }
        return Result.success(details);
    }

    @PreAuthorize("@auth.hasCode('assembly:op:rcd:remove')")
    @Operation(summary ="删除输出方案执行记录")
    @PostMapping("/removeOpRcd")
    public Result<String> removeOpRcd(@RequestBody @Valid CommonIdDto dto) throws Exception{
        opRcdService.removeOpRcd(dto);
        return Result.success("操作成功");
    }

}
