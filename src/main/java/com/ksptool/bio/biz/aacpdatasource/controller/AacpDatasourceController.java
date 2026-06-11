package com.ksptool.bio.biz.aacpdatasource.controller;

import com.ksptool.assembly.entity.web.CommonIdDto;
import com.ksptool.assembly.entity.web.PageResult;
import com.ksptool.assembly.entity.web.Result;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import com.ksptool.bio.commons.annotation.PrintLog;

import com.ksptool.bio.biz.aacpdatasource.service.AacpDatasourceService;
import com.ksptool.bio.biz.aacpdatasource.model.dto.AddAacpDatasourceDto;
import com.ksptool.bio.biz.aacpdatasource.model.dto.EditAacpDatasourceDto;
import com.ksptool.bio.biz.aacpdatasource.model.dto.GetAacpDatasourceListDto;
import com.ksptool.bio.biz.aacpdatasource.model.vo.GetAacpDatasourceListVo;
import com.ksptool.bio.biz.aacpdatasource.model.vo.GetAacpDatasourceDetailsVo;

@PrintLog
@RestController
@RequestMapping("/aacpDatasource")
@Tag(name = "AACP数据源", description = "AACP数据源")
@Slf4j
public class AacpDatasourceController {

    @Autowired
    private AacpDatasourceService aacpDatasourceService;

    @PreAuthorize("@auth.hasCode('aacp:datasource:view')")
    @PostMapping("/getAacpDatasourceList")
    @Operation(summary ="查询AACP数据源列表")
    public PageResult<GetAacpDatasourceListVo> getAacpDatasourceList(@RequestBody @Valid GetAacpDatasourceListDto dto) throws Exception{
        return aacpDatasourceService.getAacpDatasourceList(dto);
    }

    @PreAuthorize("@auth.hasCode('aacp:datasource:add')")
    @Operation(summary ="新增AACP数据源")
    @PostMapping("/addAacpDatasource")
    public Result<String> addAacpDatasource(@RequestBody @Valid AddAacpDatasourceDto dto) throws Exception{
		aacpDatasourceService.addAacpDatasource(dto);
        return Result.success("新增成功");
    }

    @PreAuthorize("@auth.hasCode('aacp:datasource:edit')")
    @Operation(summary ="编辑AACP数据源")
    @PostMapping("/editAacpDatasource")
    public Result<String> editAacpDatasource(@RequestBody @Valid EditAacpDatasourceDto dto) throws Exception{
		aacpDatasourceService.editAacpDatasource(dto);
        return Result.success("修改成功");
    }

    @PreAuthorize("@auth.hasCode('aacp:datasource:view')")
    @Operation(summary ="查询AACP数据源详情")
    @PostMapping("/getAacpDatasourceDetails")
    public Result<GetAacpDatasourceDetailsVo> getAacpDatasourceDetails(@RequestBody @Valid CommonIdDto dto) throws Exception{
        GetAacpDatasourceDetailsVo details = aacpDatasourceService.getAacpDatasourceDetails(dto);
        if(details == null){
            return Result.error("无数据");
        }
        return Result.success(details);
    }

    @PreAuthorize("@auth.hasCode('aacp:datasource:remove')")
    @Operation(summary ="删除AACP数据源")
    @PostMapping("/removeAacpDatasource")
    public Result<String> removeAacpDatasource(@RequestBody @Valid CommonIdDto dto) throws Exception{
        aacpDatasourceService.removeAacpDatasource(dto);
        return Result.success("操作成功");
    }

}
