package com.ksptool.bio.biz.aacp.controller;

import com.ksptool.assembly.entity.web.CommonIdDto;
import com.ksptool.assembly.entity.web.PageResult;
import com.ksptool.assembly.entity.web.Result;
import com.ksptool.bio.biz.aacp.model.func.dto.AddMicroFuncDto;
import com.ksptool.bio.biz.aacp.model.func.dto.EditMicroFuncDto;
import com.ksptool.bio.biz.aacp.model.func.dto.GetMicroFuncListDto;
import com.ksptool.bio.biz.aacp.model.func.vo.GetMicroFuncDetailsVo;
import com.ksptool.bio.biz.aacp.model.func.vo.GetMicroFuncListVo;
import com.ksptool.bio.biz.aacp.model.func.vo.GetMicroFuncRegistryVo;
import com.ksptool.bio.biz.aacp.service.MicroFuncService;
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

import java.util.List;

@PrintLog
@RestController
@RequestMapping("/microFunc")
@Tag(name = "微函数", description = "微函数")
@Slf4j
public class MicroFuncController {

    @Autowired
    private MicroFuncService microFuncService;

    @PreAuthorize("@auth.hasCode('aacp:func:view')")
    @PostMapping("/getMicroFuncList")
    @Operation(summary = "查询微函数列表")
    public PageResult<GetMicroFuncListVo> getMicroFuncList(@RequestBody @Valid GetMicroFuncListDto dto) throws Exception {
        return microFuncService.getMicroFuncList(dto);
    }

    @PreAuthorize("@auth.hasCode('aacp:func:add')")
    @Operation(summary = "新增微函数")
    @PostMapping("/addMicroFunc")
    public Result<String> addMicroFunc(@RequestBody @Valid AddMicroFuncDto dto) throws Exception {
        microFuncService.addMicroFunc(dto);
        return Result.success("新增成功");
    }

    @PreAuthorize("@auth.hasCode('aacp:func:edit')")
    @Operation(summary = "编辑微函数")
    @PostMapping("/editMicroFunc")
    public Result<String> editMicroFunc(@RequestBody @Valid EditMicroFuncDto dto) throws Exception {
        microFuncService.editMicroFunc(dto);
        return Result.success("修改成功");
    }

    @PreAuthorize("@auth.hasCode('aacp:func:view')")
    @Operation(summary = "查询微函数详情")
    @PostMapping("/getMicroFuncDetails")
    public Result<GetMicroFuncDetailsVo> getMicroFuncDetails(@RequestBody @Valid CommonIdDto dto) throws Exception {
        GetMicroFuncDetailsVo details = microFuncService.getMicroFuncDetails(dto);
        if (details == null) {
            return Result.error("无数据");
        }
        return Result.success(details);
    }

    @PreAuthorize("@auth.hasCode('aacp:func:remove')")
    @Operation(summary = "删除微函数")
    @PostMapping("/removeMicroFunc")
    public Result<String> removeMicroFunc(@RequestBody @Valid CommonIdDto dto) throws Exception {
        microFuncService.removeMicroFunc(dto);
        return Result.success("操作成功");
    }

    @PreAuthorize("@auth.hasCode('aacp:func:view')")
    @Operation(summary = "获取已注册微函数列表")
    @PostMapping("/getMicroFuncRegistryList")
    public Result<List<GetMicroFuncRegistryVo>> getMicroFuncRegistryList() throws Exception {
        return Result.success(microFuncService.getMicroFuncRegistryList());
    }

}
