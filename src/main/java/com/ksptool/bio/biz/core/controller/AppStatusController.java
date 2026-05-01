package com.ksptool.bio.biz.core.controller;

import com.ksptool.assembly.entity.web.Result;
import com.ksptool.bio.biz.core.model.appstatus.vo.GetRtStatusVo;
import com.ksptool.bio.biz.core.model.appstatus.vo.GetSystemInfoVo;
import com.ksptool.bio.biz.core.service.AppStatusService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/appStatus")
@Tag(name = "CORE-应用性能监控", description = "应用性能监控")
@Slf4j
public class AppStatusController {

    @Autowired
    private AppStatusService appStatusService;

    @PreAuthorize("@auth.hasCode('app:status:view')")
    @Operation(summary = "获取实时性能监控数据")
    @PostMapping("/getRtStatus")
    public Result<GetRtStatusVo> getRtStatus() {
        return Result.success(appStatusService.getRtStatus());
    }

    @PreAuthorize("@auth.hasCode('app:system:view')")
    @Operation(summary = "获取系统信息")
    @PostMapping("/getSystemInfo")
    public Result<GetSystemInfoVo> getSystemInfo() {
        return Result.success(appStatusService.getSystemInfo());
    }

}

