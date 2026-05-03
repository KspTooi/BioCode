package com.ksptool.bio.biz.rdbg.controller;

import com.ksptool.bio.biz.rdbg.model.userrequestlog.GetUserRequestLogDetailsVo;
import com.ksptool.bio.biz.rdbg.model.userrequestlog.GetUserRequestLogListDto;
import com.ksptool.bio.biz.rdbg.model.userrequestlog.GetUserRequestLogListVo;
import com.ksptool.bio.biz.rdbg.service.UserRequestLogService;
import com.ksptool.assembly.entity.web.CommonIdDto;
import com.ksptool.assembly.entity.web.PageResult;
import com.ksptool.assembly.entity.web.Result;
import com.ksptool.bio.commons.annotation.PrintLog;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@PrintLog
@RestController
@RequestMapping("/userRequestLog")
@Tag(name = "RDBG-用户请求记录", description = "用户请求记录管理")
@Slf4j
public class UserRequestLogController {

    @Autowired
    private UserRequestLogService userRequestLogService;

    @PostMapping("/getUserRequestLogList")
    @Operation(summary = "列表查询")
    public PageResult<GetUserRequestLogListVo> getUserRequestLogList(@RequestBody @Valid GetUserRequestLogListDto dto) throws Exception {
        return userRequestLogService.getUserRequestLogList(dto);
    }

    @Operation(summary = "查询详情")
    @PostMapping("/getUserRequestLogDetails")
    public Result<GetUserRequestLogDetailsVo> getUserRequestLogDetails(@RequestBody @Valid CommonIdDto dto) throws Exception {
        GetUserRequestLogDetailsVo details = userRequestLogService.getUserRequestLogDetails(dto);
        if (details == null) {
            return Result.error("无数据");
        }
        return Result.success(details);
    }

    @Operation(summary = "查询最新的响应")
    @PostMapping("/getLastUserRequestLogDetails")
    public Result<GetUserRequestLogDetailsVo> getLastUserRequestLogDetails(@RequestBody @Valid CommonIdDto dto) throws Exception {
        GetUserRequestLogDetailsVo details = userRequestLogService.getLastUserRequestLogDetails(dto);
        if (details == null) {
            return Result.error("无数据");
        }
        return Result.success(details);
    }

    @Operation(summary = "删除")
    @PostMapping("/removeUserRequestLog")
    public Result<String> removeUserRequestLog(@RequestBody @Valid CommonIdDto dto) throws Exception {
        userRequestLogService.removeUserRequestLog(dto);
        return Result.success("操作成功");
    }

}
