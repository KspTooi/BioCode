package com.ksptool.bio.biz.aacp.controller;

import com.ksptool.assembly.entity.web.CommonIdDto;
import com.ksptool.assembly.entity.web.PageResult;
import com.ksptool.assembly.entity.web.Result;
import com.ksptool.bio.biz.aacp.model.datasource.dto.AddAacpDatasourceDto;
import com.ksptool.bio.biz.aacp.model.datasource.dto.EditAacpDatasourceDto;
import com.ksptool.bio.biz.aacp.model.datasource.dto.GetAacpDatasourceListDto;
import com.ksptool.bio.biz.aacp.model.datasource.vo.GetAacpDatasourceDetailsVo;
import com.ksptool.bio.biz.aacp.model.datasource.vo.GetAacpDatasourceListVo;
import com.ksptool.bio.biz.aacp.service.AacpDatasourceService;
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
@RequestMapping("/aacpDatasource")
@Tag(name = "AACP数据源", description = "AACP数据源")
@Slf4j
public class AacpDatasourceController {

    @Autowired
    private AacpDatasourceService aacpDatasourceService;

    @PreAuthorize("@auth.hasCode('aacp:datasource:view')")
    @PostMapping("/getAacpDatasourceList")
    @Operation(summary = "查询AACP数据源列表")
    public PageResult<GetAacpDatasourceListVo> getAacpDatasourceList(@RequestBody @Valid GetAacpDatasourceListDto dto) throws Exception {
        return aacpDatasourceService.getAacpDatasourceList(dto);
    }

    @PreAuthorize("@auth.hasCode('aacp:datasource:add')")
    @PostMapping("/addAacpDatasource")
    @Operation(summary = "新增AACP数据源")
    public Result<String> addAacpDatasource(@RequestBody @Valid AddAacpDatasourceDto dto) throws Exception {
        aacpDatasourceService.addAacpDatasource(dto);
        return Result.success("新增成功");
    }

    @PreAuthorize("@auth.hasCode('aacp:datasource:edit')")
    @PostMapping("/editAacpDatasource")
    @Operation(summary = "编辑AACP数据源")
    public Result<String> editAacpDatasource(@RequestBody @Valid EditAacpDatasourceDto dto) throws Exception {
        aacpDatasourceService.editAacpDatasource(dto);
        return Result.success("修改成功");
    }

    @PreAuthorize("@auth.hasCode('aacp:datasource:view')")
    @PostMapping("/getAacpDatasourceDetails")
    @Operation(summary = "查询AACP数据源详情")
    public Result<GetAacpDatasourceDetailsVo> getAacpDatasourceDetails(@RequestBody @Valid CommonIdDto dto) throws Exception {
        GetAacpDatasourceDetailsVo details = aacpDatasourceService.getAacpDatasourceDetails(dto);
        if (details == null) {
            return Result.error("无数据");
        }
        return Result.success(details);
    }

    @PreAuthorize("@auth.hasCode('aacp:datasource:remove')")
    @PostMapping("/removeAacpDatasource")
    @Operation(summary = "删除AACP数据源")
    public Result<String> removeAacpDatasource(@RequestBody @Valid CommonIdDto dto) throws Exception {
        aacpDatasourceService.removeAacpDatasource(dto);
        return Result.success("操作成功");
    }

    @PreAuthorize("@auth.hasCode('aacp:datasource:test')")
    @PostMapping("/testAacpDatasourceConnection")
    @Operation(summary = "测试AACP数据源连接")
    public Result<String> testAacpDatasourceConnection(@RequestBody @Valid CommonIdDto dto) throws Exception {
        return aacpDatasourceService.testAacpDatasourceConnection(dto);
    }
}
