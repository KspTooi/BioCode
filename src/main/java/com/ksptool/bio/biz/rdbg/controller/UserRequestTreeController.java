package com.ksptool.bio.biz.rdbg.controller;

import com.ksptool.bio.biz.rdbg.model.userrequest.RemoveUserRequestTreeDto;
import com.ksptool.bio.biz.rdbg.model.userrequesttree.dto.AddUserRequestTreeDto;
import com.ksptool.bio.biz.rdbg.model.userrequesttree.dto.EditUserRequestTreeDto;
import com.ksptool.bio.biz.rdbg.model.userrequesttree.dto.GetUserRequestTreeDto;
import com.ksptool.bio.biz.rdbg.model.userrequesttree.dto.MoveUserRequestTreeDto;
import com.ksptool.bio.biz.rdbg.model.userrequesttree.vo.GetUserRequestTreeVo;
import com.ksptool.bio.biz.rdbg.service.UserRequestTreeService;
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

import java.util.List;

@PrintLog
@RestController
@RequestMapping("/userRequestTree")
@Tag(name = "RDBG-用户请求树", description = "用户请求树管理")
@Slf4j
public class UserRequestTreeController {

    @Autowired
    private UserRequestTreeService userRequestTreeService;


    @PostMapping("/getUserRequestTree")
    @Operation(summary = "获取用户请求树")
    public Result<List<GetUserRequestTreeVo>> getUserRequestTree(@RequestBody @Valid GetUserRequestTreeDto dto) throws Exception {
        return Result.success(userRequestTreeService.getUserRequestTree(dto));
    }

    @PostMapping("/addUserRequestTree")
    @Operation(summary = "新增用户请求树")
    public Result<String> addUserRequestTree(@RequestBody @Valid AddUserRequestTreeDto dto) throws Exception {
        userRequestTreeService.addUserRequestTree(dto);
        return Result.success("新增成功");
    }

    @PostMapping("/moveUserRequestTree")
    @Operation(summary = "移动用户请求树")
    public Result<List<GetUserRequestTreeVo>> moveUserRequestTree(@RequestBody @Valid MoveUserRequestTreeDto dto) throws Exception {
        return Result.success(userRequestTreeService.moveUserRequestTree(dto));
    }

    @PostMapping("/copyUserRequestTree")
    @Operation(summary = "复制用户请求树")
    public Result<String> copyUserRequestTree(@RequestBody @Valid CommonIdDto dto) throws Exception {
        userRequestTreeService.copyUserRequestTree(dto);
        return Result.success("复制成功");
    }


    @PostMapping("/editUserRequestTree")
    @Operation(summary = "编辑用户请求树")
    public Result<String> editUserRequestTree(@RequestBody @Valid EditUserRequestTreeDto dto) throws Exception {
        userRequestTreeService.editUserRequestTree(dto);
        return Result.success("修改成功");
    }

    @PostMapping("/removeUserRequestTree")
    @Operation(summary = "移除用户请求树对象")
    public Result<String> removeUserRequestTree(@RequestBody @Valid RemoveUserRequestTreeDto dto) throws Exception {
        userRequestTreeService.removeUserRequestTree(dto);
        return Result.success("修改成功");
    }


}
