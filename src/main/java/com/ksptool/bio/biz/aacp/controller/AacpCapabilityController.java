package com.ksptool.bio.biz.aacp.controller;

import com.ksptool.assembly.entity.web.CommonIdDto;
import com.ksptool.assembly.entity.web.PageResult;
import com.ksptool.assembly.entity.web.Result;
import com.ksptool.bio.biz.aacp.model.dto.AddAacpCapabilityDto;
import com.ksptool.bio.biz.aacp.model.dto.EditAacpCapabilityDto;
import com.ksptool.bio.biz.aacp.model.dto.GetAacpCapabilityListDto;
import com.ksptool.bio.biz.aacp.model.vo.GetAacpCapabilityDetailsVo;
import com.ksptool.bio.biz.aacp.model.vo.GetAacpCapabilityListVo;
import com.ksptool.bio.biz.aacp.service.AacpCapabilityService;
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
@RequestMapping("/aacpCapability")
@Tag(name = "能力包", description = "能力包")
@Slf4j
public class AacpCapabilityController {

    @Autowired
    private AacpCapabilityService aacpCapabilityService;

    @PreAuthorize("@auth.hasCode('aacp:capability:view')")
    @PostMapping("/getAacpCapabilityList")
    @Operation(summary = "查询能力包列表")
    public PageResult<GetAacpCapabilityListVo> getAacpCapabilityList(@RequestBody @Valid GetAacpCapabilityListDto dto) throws Exception {
        return aacpCapabilityService.getAacpCapabilityList(dto);
    }

    @PreAuthorize("@auth.hasCode('aacp:capability:add')")
    @PostMapping("/addAacpCapability")
    @Operation(summary = "新增能力包")
    public Result<String> addAacpCapability(@RequestBody @Valid AddAacpCapabilityDto dto) throws Exception {
        aacpCapabilityService.addAacpCapability(dto);
        return Result.success("新增成功");
    }

    @PreAuthorize("@auth.hasCode('aacp:capability:edit')")
    @PostMapping("/editAacpCapability")
    @Operation(summary = "编辑能力包")
    public Result<String> editAacpCapability(@RequestBody @Valid EditAacpCapabilityDto dto) throws Exception {
        aacpCapabilityService.editAacpCapability(dto);
        return Result.success("修改成功");
    }

    @PreAuthorize("@auth.hasCode('aacp:capability:view')")
    @PostMapping("/getAacpCapabilityDetails")
    @Operation(summary = "查询能力包详情")
    public Result<GetAacpCapabilityDetailsVo> getAacpCapabilityDetails(@RequestBody @Valid CommonIdDto dto) throws Exception {
        GetAacpCapabilityDetailsVo details = aacpCapabilityService.getAacpCapabilityDetails(dto);
        if (details == null) {
            return Result.error("无数据");
        }
        return Result.success(details);
    }

    @PreAuthorize("@auth.hasCode('aacp:capability:remove')")
    @PostMapping("/removeAacpCapability")
    @Operation(summary = "删除能力包")
    public Result<String> removeAacpCapability(@RequestBody @Valid CommonIdDto dto) throws Exception {
        aacpCapabilityService.removeAacpCapability(dto);
        return Result.success("操作成功");
    }

}
