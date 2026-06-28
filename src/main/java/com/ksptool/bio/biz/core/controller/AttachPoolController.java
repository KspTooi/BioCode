package com.ksptool.bio.biz.core.controller;

import com.ksptool.assembly.entity.exception.BizException;
import com.ksptool.assembly.entity.web.PageResult;
import com.ksptool.assembly.entity.web.Result;
import com.ksptool.bio.biz.core.model.attachpool.dto.GetAttachListDto;
import com.ksptool.bio.biz.core.model.attachpool.vo.GetAttachListVo;
import com.ksptool.bio.biz.core.model.attachpool.vo.GetLatestScanRecordVo;
import com.ksptool.bio.biz.core.service.AttachPoolService;
import com.ksptool.bio.commons.annotation.PrintLog;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@PrintLog
@RestController
@RequestMapping("/attachPool")
@Tag(name = "CORE-附件池管理", description = "附件池管理")
@Slf4j
public class AttachPoolController {

    @Autowired
    private AttachPoolService attachPoolService;


    @PreAuthorize("@auth.hasCode('core:attach:pool:view')")
    @Operation(summary = "查询最新的附件池扫描记录")
    @PostMapping("/getLatestScanRecord")
    public Result<GetLatestScanRecordVo> getLatestScanRecord() throws Exception {

        GetLatestScanRecordVo record = attachPoolService.getLatestScanRecord();

        if (record == null) {
            return Result.error("无数据");
        }

        return Result.success(record);
    }

    @PreAuthorize("@auth.hasCode('core:attach:pool:view')")
    @Operation(summary = "查询附件列表")
    @PostMapping("/getAttachList")
    public PageResult<GetAttachListVo> getAttachList(@RequestBody @Valid GetAttachListDto dto) {
        return attachPoolService.getAttachList(dto);
    }

    @PreAuthorize("@auth.hasCode('core:attach:pool:scan')")
    @Operation(summary = "扫描附件池")
    @PostMapping("/scanAttachPool")
    public Result<String> scanAttachPool() throws BizException {
        attachPoolService.scanAttachPool();
        return Result.success("扫描完成");
    }

}
