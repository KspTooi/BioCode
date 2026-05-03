package com.ksptool.bio.biz.rdbg.controller;

import com.ksptool.bio.biz.rdbg.model.filter.dto.AddSimpleFilterDto;
import com.ksptool.bio.biz.rdbg.model.filter.dto.EditSimpleFilterDto;
import com.ksptool.bio.biz.rdbg.model.filter.dto.GetSimpleFilterListDto;
import com.ksptool.bio.biz.rdbg.model.filter.vo.GetSimpleFilterDetailsVo;
import com.ksptool.bio.biz.rdbg.model.filter.vo.GetSimpleFilterListVo;
import com.ksptool.bio.biz.rdbg.service.SimpleFilterService;
import com.ksptool.assembly.entity.web.CommonIdDto;
import com.ksptool.assembly.entity.web.PageResult;
import com.ksptool.assembly.entity.web.Result;
import com.ksptool.bio.commons.annotation.PrintLog;
import com.ksptool.bio.commons.dataprocess.Str;
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
@RequestMapping("/simpleFilter")
@Tag(name = "RDBG-简单过滤器", description = "简单过滤器管理")
@Slf4j
public class SimpleFilterController {

    @Autowired
    private SimpleFilterService simpleFilterService;

    @PostMapping("/getSimpleFilterList")
    @Operation(summary = "查询简单过滤器列表")
    public PageResult<GetSimpleFilterListVo> getSimpleFilterList(@RequestBody @Valid GetSimpleFilterListDto dto) throws Exception {
        return simpleFilterService.getSimpleFilterList(dto);
    }

    @Operation(summary = "新增简单过滤器")
    @PostMapping("/addSimpleFilter")
    public Result<String> addSimpleFilter(@RequestBody @Valid AddSimpleFilterDto dto) throws Exception {

        //验证参数
        String validate = dto.validate();
        if (Str.isNotBlank(validate)) {
            return Result.error(validate);
        }

        return Result.success(simpleFilterService.addSimpleFilter(dto));
    }

    @Operation(summary = "编辑简单过滤器")
    @PostMapping("/editSimpleFilter")
    public Result<String> editSimpleFilter(@RequestBody @Valid EditSimpleFilterDto dto) throws Exception {

        //验证参数
        String validate = dto.validate();
        if (Str.isNotBlank(validate)) {
            return Result.error(validate);
        }

        simpleFilterService.editSimpleFilter(dto);
        return Result.success("修改成功");
    }

    @Operation(summary = "查询简单过滤器详情")
    @PostMapping("/getSimpleFilterDetails")
    public Result<GetSimpleFilterDetailsVo> getSimpleFilterDetails(@RequestBody @Valid CommonIdDto dto) throws Exception {
        GetSimpleFilterDetailsVo details = simpleFilterService.getSimpleFilterDetails(dto);
        if (details == null) {
            return Result.error("无数据");
        }
        return Result.success(details);
    }

    @Operation(summary = "删除简单过滤器")
    @PostMapping("/removeSimpleFilter")
    public Result<String> removeSimpleFilter(@RequestBody @Valid CommonIdDto dto) throws Exception {
        simpleFilterService.removeSimpleFilter(dto);
        return Result.success("操作成功");
    }

}
