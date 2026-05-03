package com.ksptool.bio.biz.assembly.controller;

import com.ksptool.assembly.entity.web.CommonIdDto;
import com.ksptool.assembly.entity.web.PageResult;
import com.ksptool.assembly.entity.web.Result;
import com.ksptool.bio.biz.assembly.model.rawmodel.dto.GetRawModelDto;
import com.ksptool.bio.biz.assembly.model.rawmodel.vo.GetRawModelListVo;
import com.ksptool.bio.biz.assembly.service.RawModelService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/rawModel")
@Tag(name = "ASSEMBLY-输出方案原始模型管理", description = "输出方案原始模型管理")
@Slf4j
public class RawModelController {

    @Autowired
    private RawModelService rawModelService;

    @PostMapping("/getRawModelList")
    @Operation(summary = "查询原始模型列表")
    public PageResult<GetRawModelListVo> getRawModelList(@RequestBody @Valid GetRawModelDto dto) throws Exception {
        return rawModelService.getRawModelList(dto);
    }

    @PostMapping("/syncRawModelFromDataSource")
    @Operation(summary = "从数据源同步原始模型")
    public Result<String> syncRawModelFromDataSource(@RequestBody @Valid CommonIdDto dto) throws Exception {
        rawModelService.syncRawModelFromDataSource(dto.getId());
        return Result.success("同步成功");
    }

}
