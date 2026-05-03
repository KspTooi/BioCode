package com.ksptool.bio.biz.rdbg.controller;

import com.ksptool.bio.biz.rdbg.model.userrequestenvstorage.dto.AddUserRequestEnvStorageDto;
import com.ksptool.bio.biz.rdbg.model.userrequestenvstorage.dto.EditUserRequestEnvStorageDto;
import com.ksptool.bio.biz.rdbg.model.userrequestenvstorage.dto.GetUserRequestEnvStorageListDto;
import com.ksptool.bio.biz.rdbg.model.userrequestenvstorage.vo.GetUserRequestEnvStorageDetailsVo;
import com.ksptool.bio.biz.rdbg.model.userrequestenvstorage.vo.GetUserRequestEnvStorageListVo;
import com.ksptool.bio.biz.rdbg.service.UserRequestEnvStorageService;
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
@RequestMapping("/userRequestEnvStorage")
@Tag(name = "RDBG-环境共享存储", description = "环境共享存储管理")
@Slf4j
public class UserRequestEnvStorageController {

    @Autowired
    private UserRequestEnvStorageService userRequestEnvStorageService;

    @PostMapping("/getUserRequestEnvStorageList")
    @Operation(summary = "查询环境共享存储列表")
    public PageResult<GetUserRequestEnvStorageListVo> getUserRequestEnvStorageList(@RequestBody @Valid GetUserRequestEnvStorageListDto dto) throws Exception {
        return userRequestEnvStorageService.getUserRequestEnvStorageList(dto);
    }

    @Operation(summary = "新增环境共享存储")
    @PostMapping("/addUserRequestEnvStorage")
    public Result<String> addUserRequestEnvStorage(@RequestBody @Valid AddUserRequestEnvStorageDto dto) throws Exception {
        userRequestEnvStorageService.addUserRequestEnvStorage(dto);
        return Result.success("新增成功");
    }

    @Operation(summary = "编辑环境共享存储")
    @PostMapping("/editUserRequestEnvStorage")
    public Result<String> editUserRequestEnvStorage(@RequestBody @Valid EditUserRequestEnvStorageDto dto) throws Exception {
        userRequestEnvStorageService.editUserRequestEnvStorage(dto);
        return Result.success("修改成功");
    }

    @Operation(summary = "查询环境共享存储详情")
    @PostMapping("/getUserRequestEnvStorageDetails")
    public Result<GetUserRequestEnvStorageDetailsVo> getUserRequestEnvStorageDetails(@RequestBody @Valid CommonIdDto dto) throws Exception {
        GetUserRequestEnvStorageDetailsVo details = userRequestEnvStorageService.getUserRequestEnvStorageDetails(dto);
        if (details == null) {
            return Result.error("无数据");
        }
        return Result.success(details);
    }

    @Operation(summary = "删除环境共享存储")
    @PostMapping("/removeUserRequestEnvStorage")
    public Result<String> removeUserRequestEnvStorage(@RequestBody @Valid CommonIdDto dto) throws Exception {
        userRequestEnvStorageService.removeUserRequestEnvStorage(dto);
        return Result.success("操作成功");
    }

}
