package com.ksptool.bio.biz.rdbg.controller;

import com.ksptool.bio.biz.rdbg.model.userrequestgroup.EditUserRequestGroupDto;
import com.ksptool.bio.biz.rdbg.model.userrequestgroup.GetUserRequestGroupDetailsVo;
import com.ksptool.bio.biz.rdbg.service.UserRequestGroupService;
import com.ksptool.assembly.entity.web.CommonIdDto;
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
@RequestMapping("/userRequestGroup")
@Tag(name = "RDBG-请求组中的请求", description = "请求组中的请求管理")
@Slf4j
public class UserRequestGroupController {

    @Autowired
    private UserRequestGroupService userRequestGroupService;


    @Operation(summary = "查询请求组详情")
    @PostMapping("/getUserRequestGroupDetails")
    public Result<GetUserRequestGroupDetailsVo> getUserRequestGroupDetails(@RequestBody @Valid CommonIdDto dto) throws Exception {
        GetUserRequestGroupDetailsVo details = userRequestGroupService.getUserRequestGroupDetails(dto);
        if (details == null) {
            return Result.error("无数据");
        }
        return Result.success(details);
    }


    @Operation(summary = "编辑请求组")
    @PostMapping("/editUserRequestGroup")
    public Result<String> editUserRequestGroup(@RequestBody @Valid EditUserRequestGroupDto dto) throws Exception {
        userRequestGroupService.editUserRequestGroup(dto);
        return Result.success("修改成功");
    }


}
