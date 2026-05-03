package com.ksptool.bio.biz.rdbg.controller;

import com.ksptool.bio.biz.rdbg.model.filter.dto.AddSimpleFilterTriggerDto;
import com.ksptool.bio.biz.rdbg.model.filter.dto.EditSimpleFilterTriggerDto;
import com.ksptool.bio.biz.rdbg.model.filter.dto.GetSimpleFilterTriggerListDto;
import com.ksptool.bio.biz.rdbg.model.filter.vo.GetSimpleFilterTriggerDetailsVo;
import com.ksptool.bio.biz.rdbg.model.filter.vo.GetSimpleFilterTriggerListVo;
import com.ksptool.bio.biz.rdbg.service.SimpleFilterTriggerService;
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
@RequestMapping("/simpleFilterTrigger")
@Tag(name = "RDBG-简单过滤器触发器", description = "简单过滤器触发器管理")
@Slf4j
public class SimpleFilterTriggerController {

    @Autowired
    private SimpleFilterTriggerService simpleFilterTriggerService;

    @PostMapping("/getSimpleFilterTriggerList")
    @Operation(summary = "查询简单过滤器触发器列表")
    public PageResult<GetSimpleFilterTriggerListVo> getSimpleFilterTriggerList(@RequestBody @Valid GetSimpleFilterTriggerListDto dto) throws Exception {
        return simpleFilterTriggerService.getSimpleFilterTriggerList(dto);
    }

    @Operation(summary = "新增简单过滤器触发器")
    @PostMapping("/addSimpleFilterTrigger")
    public Result<String> addSimpleFilterTrigger(@RequestBody @Valid AddSimpleFilterTriggerDto dto) throws Exception {
        simpleFilterTriggerService.addSimpleFilterTrigger(dto);
        return Result.success("新增成功");
    }

    @Operation(summary = "编辑简单过滤器触发器")
    @PostMapping("/editSimpleFilterTrigger")
    public Result<String> editSimpleFilterTrigger(@RequestBody @Valid EditSimpleFilterTriggerDto dto) throws Exception {
        simpleFilterTriggerService.editSimpleFilterTrigger(dto);
        return Result.success("修改成功");
    }

    @Operation(summary = "查询简单过滤器触发器详情")
    @PostMapping("/getSimpleFilterTriggerDetails")
    public Result<GetSimpleFilterTriggerDetailsVo> getSimpleFilterTriggerDetails(@RequestBody @Valid CommonIdDto dto) throws Exception {
        GetSimpleFilterTriggerDetailsVo details = simpleFilterTriggerService.getSimpleFilterTriggerDetails(dto);
        if (details == null) {
            return Result.error("无数据");
        }
        return Result.success(details);
    }

    @Operation(summary = "删除简单过滤器触发器")
    @PostMapping("/removeSimpleFilterTrigger")
    public Result<String> removeSimpleFilterTrigger(@RequestBody @Valid CommonIdDto dto) throws Exception {
        simpleFilterTriggerService.removeSimpleFilterTrigger(dto);
        return Result.success("操作成功");
    }

}
