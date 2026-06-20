package com.ksptool.bio.biz.aacp.controller;

import com.ksptool.assembly.entity.web.CommonIdDto;
import com.ksptool.assembly.entity.web.PageResult;
import com.ksptool.assembly.entity.web.Result;
import com.ksptool.bio.biz.aacp.model.aacpapp.dto.AddAacpAppDto;
import com.ksptool.bio.biz.aacp.model.aacpapp.dto.EditAacpAppDto;
import com.ksptool.bio.biz.aacp.model.aacpapp.dto.GetAacpAppListDto;
import com.ksptool.bio.biz.aacp.model.aacpapp.vo.GetAacpAppDetailsVo;
import com.ksptool.bio.biz.aacp.model.aacpapp.vo.GetAacpAppListVo;
import com.ksptool.bio.biz.aacp.service.AacpAppService;
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
@RequestMapping("/aacpApp")
@Tag(name = "AACP应用", description = "AACP应用")
@Slf4j
public class AacpAppController {

    @Autowired
    private AacpAppService aacpAppService;

    @PreAuthorize("@auth.hasCode('aacp:app:view')")
    @PostMapping("/getAacpAppList")
    @Operation(summary = "查询AACP应用列表")
    public PageResult<GetAacpAppListVo> getAacpAppList(@RequestBody @Valid GetAacpAppListDto dto) throws Exception {
        return aacpAppService.getAacpAppList(dto);
    }

    @PreAuthorize("@auth.hasCode('aacp:app:add')")
    @Operation(summary = "新增AACP应用")
    @PostMapping("/addAacpApp")
    public Result<String> addAacpApp(@RequestBody @Valid AddAacpAppDto dto) throws Exception {
        aacpAppService.addAacpApp(dto);
        return Result.success("新增成功");
    }

    @PreAuthorize("@auth.hasCode('aacp:app:edit')")
    @Operation(summary = "编辑AACP应用")
    @PostMapping("/editAacpApp")
    public Result<String> editAacpApp(@RequestBody @Valid EditAacpAppDto dto) throws Exception {
        aacpAppService.editAacpApp(dto);
        return Result.success("修改成功");
    }

    @PreAuthorize("@auth.hasCode('aacp:app:view')")
    @Operation(summary = "查询AACP应用详情")
    @PostMapping("/getAacpAppDetails")
    public Result<GetAacpAppDetailsVo> getAacpAppDetails(@RequestBody @Valid CommonIdDto dto) throws Exception {
        GetAacpAppDetailsVo details = aacpAppService.getAacpAppDetails(dto);
        if (details == null) {
            return Result.error("无数据");
        }
        return Result.success(details);
    }

    @PreAuthorize("@auth.hasCode('aacp:app:remove')")
    @Operation(summary = "删除AACP应用")
    @PostMapping("/removeAacpApp")
    public Result<String> removeAacpApp(@RequestBody @Valid CommonIdDto dto) throws Exception {
        aacpAppService.removeAacpApp(dto);
        return Result.success("操作成功");
    }

}
