package com.ksptool.bio.biz.rdbg.controller;

import com.ksptool.bio.biz.rdbg.model.filter.dto.AddSimpleFilterOperationDto;
import com.ksptool.bio.biz.rdbg.model.filter.dto.EditSimpleFilterOperationDto;
import com.ksptool.bio.biz.rdbg.model.filter.dto.GetSimpleFilterOperationListDto;
import com.ksptool.bio.biz.rdbg.model.filter.vo.GetSimpleFilterOperationDetailsVo;
import com.ksptool.bio.biz.rdbg.model.filter.vo.GetSimpleFilterOperationListVo;
import com.ksptool.bio.biz.rdbg.service.SimpleFilterOperationService;
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
@RequestMapping("/simpleFilterOperation")
@Tag(name = "RDBG-简单过滤器操作", description = "简单过滤器操作管理")
@Slf4j
public class SimpleFilterOperationController {

    @Autowired
    private SimpleFilterOperationService simpleFilterOperationService;

    @PostMapping("/getSimpleFilterOperationList")
    @Operation(summary = "列表查询")
    public PageResult<GetSimpleFilterOperationListVo> getSimpleFilterOperationList(@RequestBody @Valid GetSimpleFilterOperationListDto dto) throws Exception {
        return simpleFilterOperationService.getSimpleFilterOperationList(dto);
    }

    @Operation(summary = "新增")
    @PostMapping("/addSimpleFilterOperation")
    public Result<String> addSimpleFilterOperation(@RequestBody @Valid AddSimpleFilterOperationDto dto) throws Exception {
        simpleFilterOperationService.addSimpleFilterOperation(dto);
        return Result.success("新增成功");
    }

    @Operation(summary = "编辑")
    @PostMapping("/editSimpleFilterOperation")
    public Result<String> editSimpleFilterOperation(@RequestBody @Valid EditSimpleFilterOperationDto dto) throws Exception {
        simpleFilterOperationService.editSimpleFilterOperation(dto);
        return Result.success("修改成功");
    }

    @Operation(summary = "查询详情")
    @PostMapping("/getSimpleFilterOperationDetails")
    public Result<GetSimpleFilterOperationDetailsVo> getSimpleFilterOperationDetails(@RequestBody @Valid CommonIdDto dto) throws Exception {
        GetSimpleFilterOperationDetailsVo details = simpleFilterOperationService.getSimpleFilterOperationDetails(dto);
        if (details == null) {
            return Result.error("无数据");
        }
        return Result.success(details);
    }

    @Operation(summary = "删除")
    @PostMapping("/removeSimpleFilterOperation")
    public Result<String> removeSimpleFilterOperation(@RequestBody @Valid CommonIdDto dto) throws Exception {
        simpleFilterOperationService.removeSimpleFilterOperation(dto);
        return Result.success("操作成功");
    }

}
