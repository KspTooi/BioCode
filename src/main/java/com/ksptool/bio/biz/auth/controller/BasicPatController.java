package com.ksptool.bio.biz.auth.controller;

import com.ksptool.assembly.entity.web.CommonIdDto;
import com.ksptool.assembly.entity.web.PageResult;
import com.ksptool.assembly.entity.web.Result;
import com.ksptool.bio.biz.auth.common.aop.RowScope;
import com.ksptool.bio.biz.auth.model.basicpat.dto.AddBasicPatDto;
import com.ksptool.bio.biz.auth.model.basicpat.dto.EditBasicPatDto;
import com.ksptool.bio.biz.auth.model.basicpat.dto.GetBasicPatListDto;
import com.ksptool.bio.biz.auth.model.basicpat.vo.GetBasicPatDetailsVo;
import com.ksptool.bio.biz.auth.model.basicpat.vo.GetBasicPatListVo;
import com.ksptool.bio.biz.auth.service.BasicPatService;
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
@RequestMapping("/basicPat")
@Tag(name = "AUTH-基本PAT", description = "基本PAT")
@Slf4j
@RowScope(mode = RowScope.Mode.ROOT_ONLY)
public class BasicPatController {

    @Autowired
    private BasicPatService basicPatService;

    @PreAuthorize("@auth.hasCode('auth:basic:pat:view')")
    @PostMapping("/getBasicPatList")
    @Operation(summary = "查询基本PAT列表")
    public PageResult<GetBasicPatListVo> getBasicPatList(@RequestBody @Valid GetBasicPatListDto dto) throws Exception {
        return basicPatService.getBasicPatList(dto);
    }

    @PreAuthorize("@auth.hasCode('auth:basic:pat:add')")
    @PostMapping("/addBasicPat")
    @Operation(summary = "新增基本PAT")
    public Result<String> addBasicPat(@RequestBody @Valid AddBasicPatDto dto) throws Exception {
        basicPatService.addBasicPat(dto);
        return Result.success("新增成功");
    }

    @PreAuthorize("@auth.hasCode('auth:basic:pat:edit')")
    @PostMapping("/editBasicPat")
    @Operation(summary = "编辑基本PAT")
    public Result<String> editBasicPat(@RequestBody @Valid EditBasicPatDto dto) throws Exception {
        basicPatService.editBasicPat(dto);
        return Result.success("修改成功");
    }

    @PreAuthorize("@auth.hasCode('auth:basic:pat:view')")
    @PostMapping("/getBasicPatDetails")
    @Operation(summary = "查询基本PAT详情")
    public Result<GetBasicPatDetailsVo> getBasicPatDetails(@RequestBody @Valid CommonIdDto dto) throws Exception {
        GetBasicPatDetailsVo details = basicPatService.getBasicPatDetails(dto);
        if (details == null) {
            return Result.error("无数据");
        }
        return Result.success(details);
    }

    @PreAuthorize("@auth.hasCode('auth:basic:pat:remove')")
    @PostMapping("/removeBasicPat")
    @Operation(summary = "删除基本PAT")
    public Result<String> removeBasicPat(@RequestBody @Valid CommonIdDto dto) throws Exception {
        basicPatService.removeBasicPat(dto);
        return Result.success("操作成功");
    }
}
