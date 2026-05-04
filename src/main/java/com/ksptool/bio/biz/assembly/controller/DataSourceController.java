package com.ksptool.bio.biz.assembly.controller;

import com.ksptool.assembly.entity.exception.BizException;
import com.ksptool.assembly.entity.web.CommonIdDto;
import com.ksptool.assembly.entity.web.PageResult;
import com.ksptool.assembly.entity.web.Result;

import java.util.List;

import com.ksptool.bio.biz.assembly.model.datsource.dto.AddDataSourceDto;
import com.ksptool.bio.biz.assembly.model.datsource.dto.EditDataSourceDto;
import com.ksptool.bio.biz.assembly.model.datsource.dto.GetDataSourceListDto;
import com.ksptool.bio.biz.assembly.model.datsource.vo.GetDataSourceDetailsVo;
import com.ksptool.bio.biz.assembly.model.datsource.vo.GetDataSourceListVo;
import com.ksptool.bio.biz.assembly.model.datsource.vo.GetDataSourceTableListVo;
import com.ksptool.bio.biz.assembly.service.DataSourceService;
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


@RestController
@RequestMapping("/dataSource")
@Tag(name = "ASSEMBLY-数据源管理", description = "数据源管理")
@Slf4j
public class DataSourceController {

    @Autowired
    private DataSourceService dataSourceService;

    @PostMapping("/getDataSourceList")
    @Operation(summary = "查询数据源列表")
    public PageResult<GetDataSourceListVo> getDataSourceList(@RequestBody @Valid GetDataSourceListDto dto) throws Exception {
        return dataSourceService.getDataSourceList(dto);
    }

    @PreAuthorize("@auth.hasCode('assembly:dataSource:add')")
    @Operation(summary = "新增数据源")
    @PostMapping("/addDataSource")
    public Result<String> addDataSource(@RequestBody @Valid AddDataSourceDto dto) throws Exception {
        dataSourceService.addDataSource(dto);
        return Result.success("新增成功");
    }

    @PreAuthorize("@auth.hasCode('assembly:dataSource:edit')")
    @Operation(summary = "编辑数据源")
    @PostMapping("/editDataSource")
    public Result<String> editDataSource(@RequestBody @Valid EditDataSourceDto dto) throws Exception {
        dataSourceService.editDataSource(dto);
        return Result.success("修改成功");
    }

    @PreAuthorize("@auth.hasCode('assembly:dataSource:view')")
    @Operation(summary = "查询数据源详情")
    @PostMapping("/getDataSourceDetails")
    public Result<GetDataSourceDetailsVo> getDataSourceDetails(@RequestBody @Valid CommonIdDto dto) throws Exception {
        GetDataSourceDetailsVo details = dataSourceService.getDataSourceDetails(dto);
        if (details == null) {
            return Result.error("无数据");
        }
        return Result.success(details);
    }

    @PreAuthorize("@auth.hasCode('assembly:dataSource:remove')")
    @Operation(summary = "删除数据源")
    @PostMapping("/removeDataSource")
    public Result<String> removeDataSource(@RequestBody @Valid CommonIdDto dto) throws Exception {

        if (dto.isBatch()) {
            throw new BizException("不支持批量操作.");
        }

        dataSourceService.removeDataSource(dto);
        return Result.success("操作成功");
    }

    @PreAuthorize("@auth.hasCode('assembly:dataSource:test')")
    @Operation(summary = "测试数据源连接")
    @PostMapping("/testDataSourceConnection")
    public Result<String> testDataSourceConnection(@RequestBody @Valid CommonIdDto dto) throws Exception {
        return dataSourceService.testDataSourceConnection(dto);
    }

    @Operation(summary = "查询数据源表列表")
    @PostMapping("/getDataSourceTableList")
    public Result<List<GetDataSourceTableListVo>> getDataSourceTableList(@RequestBody @Valid CommonIdDto dto) throws Exception {
        return Result.success(dataSourceService.getDataSourceTableList(dto));
    }

}
