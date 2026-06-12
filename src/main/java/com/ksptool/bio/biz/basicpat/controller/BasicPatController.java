package com.ksptool.bio.biz.basicpat.controller;

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

import com.ksptool.bio.biz.basicpat.service.BasicPatService;
import com.ksptool.bio.biz.basicpat.model.dto.AddBasicPatDto;
import com.ksptool.bio.biz.basicpat.model.dto.EditBasicPatDto;
import com.ksptool.bio.biz.basicpat.model.dto.GetBasicPatListDto;
import com.ksptool.bio.biz.basicpat.model.vo.GetBasicPatListVo;
import com.ksptool.bio.biz.basicpat.model.vo.GetBasicPatDetailsVo;

@PrintLog
@RestController
@RequestMapping("/basicPat")
@Tag(name = "基本PAT", description = "基本PAT")
@Slf4j
public class BasicPatController {

    @Autowired
    private BasicPatService basicPatService;

    @PreAuthorize("@auth.hasCode('auth:basic:pat:view')")
    @PostMapping("/getBasicPatList")
    @Operation(summary ="查询基本PAT列表")
    public PageResult<GetBasicPatListVo> getBasicPatList(@RequestBody @Valid GetBasicPatListDto dto) throws Exception{
        return basicPatService.getBasicPatList(dto);
    }

    @PreAuthorize("@auth.hasCode('auth:basic:pat:add')")
    @Operation(summary ="新增基本PAT")
    @PostMapping("/addBasicPat")
    public Result<String> addBasicPat(@RequestBody @Valid AddBasicPatDto dto) throws Exception{
		basicPatService.addBasicPat(dto);
        return Result.success("新增成功");
    }

    @PreAuthorize("@auth.hasCode('auth:basic:pat:edit')")
    @Operation(summary ="编辑基本PAT")
    @PostMapping("/editBasicPat")
    public Result<String> editBasicPat(@RequestBody @Valid EditBasicPatDto dto) throws Exception{
		basicPatService.editBasicPat(dto);
        return Result.success("修改成功");
    }

    @PreAuthorize("@auth.hasCode('auth:basic:pat:view')")
    @Operation(summary ="查询基本PAT详情")
    @PostMapping("/getBasicPatDetails")
    public Result<GetBasicPatDetailsVo> getBasicPatDetails(@RequestBody @Valid CommonIdDto dto) throws Exception{
        GetBasicPatDetailsVo details = basicPatService.getBasicPatDetails(dto);
        if(details == null){
            return Result.error("无数据");
        }
        return Result.success(details);
    }

    @PreAuthorize("@auth.hasCode('auth:basic:pat:remove')")
    @Operation(summary ="删除基本PAT")
    @PostMapping("/removeBasicPat")
    public Result<String> removeBasicPat(@RequestBody @Valid CommonIdDto dto) throws Exception{
        basicPatService.removeBasicPat(dto);
        return Result.success("操作成功");
    }

}
