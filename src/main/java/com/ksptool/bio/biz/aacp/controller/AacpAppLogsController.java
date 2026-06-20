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

import com.ksptool.bio.biz.aacp.service.AacpAppLogsService;
import com.ksptool.bio.biz.aacp.model.applogs.dto.GetAppLogsListDto;
import com.ksptool.bio.biz.aacp.model.applogs.vo.GetAppLogsListVo;
import com.ksptool.bio.biz.aacp.model.applogs.vo.GetAppLogsDetailsVo;

@PrintLog
@RestController
@RequestMapping("/appLogs")
@Tag(name = "模型调用记录", description = "模型调用记录")
@Slf4j
public class AacpAppLogsController {

    @Autowired
    private AacpAppLogsService aacpAppLogsService;

    @PreAuthorize("@auth.hasCode('aacp:app:logs:view')")
    @PostMapping("/getAppLogsList")
    @Operation(summary ="查询模型调用记录列表")
    public PageResult<GetAppLogsListVo> getAppLogsList(@RequestBody @Valid GetAppLogsListDto dto) throws Exception{
        return aacpAppLogsService.getAppLogsList(dto);
    }

    @PreAuthorize("@auth.hasCode('aacp:app:logs:view')")
    @Operation(summary ="查询模型调用记录详情")
    @PostMapping("/getAppLogsDetails")
    public Result<GetAppLogsDetailsVo> getAppLogsDetails(@RequestBody @Valid CommonIdDto dto) throws Exception{
        GetAppLogsDetailsVo details = aacpAppLogsService.getAppLogsDetails(dto);
        if(details == null){
            return Result.error("无数据");
        }
        return Result.success(details);
    }

    @PreAuthorize("@auth.hasCode('aacp:app:logs:remove')")
    @Operation(summary ="删除模型调用记录")
    @PostMapping("/removeAppLogs")
    public Result<String> removeAppLogs(@RequestBody @Valid CommonIdDto dto) throws Exception{
        aacpAppLogsService.removeAppLogs(dto);
        return Result.success("操作成功");
    }

}
