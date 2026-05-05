package com.ksptool.bio.biz.qt.controller;

import com.ksptool.assembly.entity.web.CommonIdDto;
import com.ksptool.assembly.entity.web.PageResult;
import com.ksptool.assembly.entity.web.Result;
import com.ksptool.bio.biz.qt.model.qttaskrcd.dto.GetQtTaskRcdListDto;
import com.ksptool.bio.biz.qt.model.qttaskrcd.vo.GetQtTaskRcdDetailsVo;
import com.ksptool.bio.biz.qt.model.qttaskrcd.vo.GetQtTaskRcdListVo;
import com.ksptool.bio.biz.qt.service.QtTaskRcdService;
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

/**
 * 任务调度日志
 *
 * @author KspTool(ksptool@outlook.com)
 * @license Apache License 2.0
 * @since 1.6.1(A).1
 */
@RestController
@RequestMapping("/qtTaskRcd")
@Tag(name = "QT-任务调度日志", description = "任务调度日志模块")
@Slf4j
public class QtTaskRcdController {

    @Autowired
    private QtTaskRcdService qtTaskRcdService;

    @PreAuthorize("@auth.hasCode('qt:rcd:list')")
    @PostMapping("/getQtTaskRcdList")
    @Operation(summary = "查询调度日志列表")
    public PageResult<GetQtTaskRcdListVo> getQtTaskRcdList(@RequestBody @Valid GetQtTaskRcdListDto dto) throws Exception {
        return qtTaskRcdService.getQtTaskRcdList(dto);
    }

    @PreAuthorize("@auth.hasCode('qt:rcd:details')")
    @Operation(summary = "查询调度日志详情")
    @PostMapping("/getQtTaskRcdDetails")
    public Result<GetQtTaskRcdDetailsVo> getQtTaskRcdDetails(@RequestBody @Valid CommonIdDto dto) throws Exception {
        GetQtTaskRcdDetailsVo details = qtTaskRcdService.getQtTaskRcdDetails(dto);
        if (details == null) {
            return Result.error("无数据");
        }
        return Result.success(details);
    }

    @PreAuthorize("@auth.hasCode('qt:rcd:remove')")
    @Operation(summary = "删除调度日志")
    @PostMapping("/removeQtTaskRcd")
    public Result<String> removeQtTaskRcd(@RequestBody @Valid CommonIdDto dto) throws Exception {
        qtTaskRcdService.removeQtTaskRcd(dto);
        return Result.success("操作成功");
    }

}
