package com.ksptool.bio.biz.core.controller;

import com.ksptool.assembly.entity.web.CommonIdDto;
import com.ksptool.assembly.entity.web.PageResult;
import com.ksptool.assembly.entity.web.Result;
import com.ksptool.bio.biz.core.model.root.dto.AddCoreRootDto;
import com.ksptool.bio.biz.core.model.root.dto.EditCoreRootDto;
import com.ksptool.bio.biz.core.model.root.dto.GetCoreRootListDto;
import com.ksptool.bio.biz.core.model.root.vo.GetCoreRootDetailsVo;
import com.ksptool.bio.biz.core.model.root.vo.GetCoreRootListVo;
import com.ksptool.bio.biz.core.service.CoreRootService;
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
@RequestMapping("/coreRoot")
@Tag(name = "CORE-租户管理", description = "租户管理")
@Slf4j
public class CoreRootController {

    @Autowired
    private CoreRootService coreRootService;

    @PreAuthorize("@auth.hasCode('core:root:view')")
    @PostMapping("/getCoreRootList")
    @Operation(summary = "查询租户列表")
    public PageResult<GetCoreRootListVo> getCoreRootList(@RequestBody @Valid GetCoreRootListDto dto) throws Exception {
        return coreRootService.getCoreRootList(dto);
    }

    @PreAuthorize("@auth.hasCode('core:root:add')")
    @Operation(summary = "新增租户")
    @PostMapping("/addCoreRoot")
    public Result<String> addCoreRoot(@RequestBody @Valid AddCoreRootDto dto) throws Exception {
        coreRootService.addCoreRoot(dto);
        return Result.success("新增成功");
    }

    @PreAuthorize("@auth.hasCode('core:root:edit')")
    @Operation(summary = "编辑租户")
    @PostMapping("/editCoreRoot")
    public Result<String> editCoreRoot(@RequestBody @Valid EditCoreRootDto dto) throws Exception {
        coreRootService.editCoreRoot(dto);
        return Result.success("修改成功");
    }

    @PreAuthorize("@auth.hasCode('core:root:view')")
    @Operation(summary = "查询租户详情")
    @PostMapping("/getCoreRootDetails")
    public Result<GetCoreRootDetailsVo> getCoreRootDetails(@RequestBody @Valid CommonIdDto dto) throws Exception {
        GetCoreRootDetailsVo details = coreRootService.getCoreRootDetails(dto);
        if (details == null) {
            return Result.error("无数据");
        }
        return Result.success(details);
    }

    @PreAuthorize("@auth.hasCode('core:root:remove')")
    @Operation(summary = "删除租户")
    @PostMapping("/removeCoreRoot")
    public Result<String> removeCoreRoot(@RequestBody @Valid CommonIdDto dto) throws Exception {
        coreRootService.removeCoreRoot(dto);
        return Result.success("操作成功");
    }

}
