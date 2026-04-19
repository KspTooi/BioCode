package com.ksptool.bio.biz.qfcc.controller;

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

import com.ksptool.bio.biz.qfcc.service.QfCcService;
import com.ksptool.bio.biz.qfcc.model.dto.AddQfCcDto;
import com.ksptool.bio.biz.qfcc.model.dto.EditQfCcDto;
import com.ksptool.bio.biz.qfcc.model.dto.GetQfCcListDto;
import com.ksptool.bio.biz.qfcc.model.vo.GetQfCcListVo;
import com.ksptool.bio.biz.qfcc.model.vo.GetQfCcDetailsVo;

@PrintLog
@RestController
@RequestMapping("/qfCc")
@Tag(name = "抄送", description = "抄送")
@Slf4j
public class QfCcController {

    @Autowired
    private QfCcService qfCcService;

    @PreAuthorize("@auth.hasCode('qf:cc:view')")
    @PostMapping("/getQfCcList")
    @Operation(summary ="查询抄送列表")
    public PageResult<GetQfCcListVo> getQfCcList(@RequestBody @Valid GetQfCcListDto dto) throws Exception{
        return qfCcService.getQfCcList(dto);
    }

    @PreAuthorize("@auth.hasCode('qf:cc:add')")
    @Operation(summary ="新增抄送")
    @PostMapping("/addQfCc")
    public Result<String> addQfCc(@RequestBody @Valid AddQfCcDto dto) throws Exception{
		qfCcService.addQfCc(dto);
        return Result.success("新增成功");
    }

    @PreAuthorize("@auth.hasCode('qf:cc:edit')")
    @Operation(summary ="编辑抄送")
    @PostMapping("/editQfCc")
    public Result<String> editQfCc(@RequestBody @Valid EditQfCcDto dto) throws Exception{
		qfCcService.editQfCc(dto);
        return Result.success("修改成功");
    }

    @PreAuthorize("@auth.hasCode('qf:cc:view')")
    @Operation(summary ="查询抄送详情")
    @PostMapping("/getQfCcDetails")
    public Result<GetQfCcDetailsVo> getQfCcDetails(@RequestBody @Valid CommonIdDto dto) throws Exception{
        GetQfCcDetailsVo details = qfCcService.getQfCcDetails(dto);
        if(details == null){
            return Result.error("无数据");
        }
        return Result.success(details);
    }

    @PreAuthorize("@auth.hasCode('qf:cc:remove')")
    @Operation(summary ="删除抄送")
    @PostMapping("/removeQfCc")
    public Result<String> removeQfCc(@RequestBody @Valid CommonIdDto dto) throws Exception{
        qfCcService.removeQfCc(dto);
        return Result.success("操作成功");
    }

}
