package com.ksptool.bio.biz.rdbg.controller;

import com.ksptool.bio.biz.rdbg.model.userrequestenv.dto.AddUserRequestEnvDto;
import com.ksptool.bio.biz.rdbg.model.userrequestenv.dto.EditUserRequestEnvDto;
import com.ksptool.bio.biz.rdbg.model.userrequestenv.dto.GetUserRequestEnvListDto;
import com.ksptool.bio.biz.rdbg.model.userrequestenv.vo.GetUserRequestEnvDetailsVo;
import com.ksptool.bio.biz.rdbg.model.userrequestenv.vo.GetUserRequestEnvListVo;
import com.ksptool.bio.biz.rdbg.service.UserRequestEnvService;
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
@RequestMapping("/userRequestEnv")
@Tag(name = "RDBG-用户请求环境", description = "用户请求环境管理")
@Slf4j
public class UserRequestEnvController {

    @Autowired
    private UserRequestEnvService userRequestEnvService;

    @PostMapping("/getUserRequestEnvList")
    @Operation(summary = "列表查询当前用户的环境")
    public PageResult<GetUserRequestEnvListVo> getUserRequestEnvList(@RequestBody @Valid GetUserRequestEnvListDto dto) throws Exception {
        return userRequestEnvService.getUserRequestEnvList(dto);
    }

    @Operation(summary = "新增当前用户的环境")
    @PostMapping("/addUserRequestEnv")
    public Result<String> addUserRequestEnv(@RequestBody @Valid AddUserRequestEnvDto dto) throws Exception {
        userRequestEnvService.addUserRequestEnv(dto);
        return Result.success("新增成功");
    }

    @Operation(summary = "编辑当前用户的环境")
    @PostMapping("/editUserRequestEnv")
    public Result<String> editUserRequestEnv(@RequestBody @Valid EditUserRequestEnvDto dto) throws Exception {
        userRequestEnvService.editUserRequestEnv(dto);
        return Result.success("修改成功");
    }

    @Operation(summary = "查询当前用户的环境详情")
    @PostMapping("/getUserRequestEnvDetails")
    public Result<GetUserRequestEnvDetailsVo> getUserRequestEnvDetails(@RequestBody @Valid CommonIdDto dto) throws Exception {
        GetUserRequestEnvDetailsVo details = userRequestEnvService.getUserRequestEnvDetails(dto);
        if (details == null) {
            return Result.error("无数据");
        }
        return Result.success(details);
    }

    @Operation(summary = "删除当前用户的环境")
    @PostMapping("/removeUserRequestEnv")
    public Result<String> removeUserRequestEnv(@RequestBody @Valid CommonIdDto dto) throws Exception {
        userRequestEnvService.removeUserRequestEnv(dto);
        return Result.success("操作成功");
    }

    @Operation(summary = "激活环境")
    @PostMapping("/activateUserRequestEnv")
    public Result<String> activateUserRequestEnv(@RequestBody @Valid CommonIdDto dto) throws Exception {
        userRequestEnvService.activateUserRequestEnv(dto);
        return Result.success("激活成功");
    }

}
