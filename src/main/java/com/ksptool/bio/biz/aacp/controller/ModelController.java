package com.ksptool.bio.biz.aacp.controller;

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

import com.ksptool.bio.biz.aacp.service.ModelService;
import com.ksptool.bio.biz.aacp.model.model.dto.AddModelDto;
import com.ksptool.bio.biz.aacp.model.model.dto.EditModelDto;
import com.ksptool.bio.biz.aacp.model.model.dto.GetModelListDto;
import com.ksptool.bio.biz.aacp.model.model.vo.GetModelListVo;
import com.ksptool.bio.biz.aacp.model.model.vo.GetModelDetailsVo;

@PrintLog
@RestController
@RequestMapping("/model")
@Tag(name = "模型变体", description = "模型变体")
@Slf4j
public class ModelController {

    @Autowired
    private ModelService modelService;

    @PreAuthorize("@auth.hasCode('aacp:model:view')")
    @PostMapping("/getModelList")
    @Operation(summary ="查询模型变体列表")
    public PageResult<GetModelListVo> getModelList(@RequestBody @Valid GetModelListDto dto) throws Exception{
        return modelService.getModelList(dto);
    }

    @PreAuthorize("@auth.hasCode('aacp:model:add')")
    @Operation(summary ="新增模型变体")
    @PostMapping("/addModel")
    public Result<String> addModel(@RequestBody @Valid AddModelDto dto) throws Exception{
		modelService.addModel(dto);
        return Result.success("新增成功");
    }

    @PreAuthorize("@auth.hasCode('aacp:model:edit')")
    @Operation(summary ="编辑模型变体")
    @PostMapping("/editModel")
    public Result<String> editModel(@RequestBody @Valid EditModelDto dto) throws Exception{
		modelService.editModel(dto);
        return Result.success("修改成功");
    }

    @PreAuthorize("@auth.hasCode('aacp:model:view')")
    @Operation(summary ="查询模型变体详情")
    @PostMapping("/getModelDetails")
    public Result<GetModelDetailsVo> getModelDetails(@RequestBody @Valid CommonIdDto dto) throws Exception{
        GetModelDetailsVo details = modelService.getModelDetails(dto);
        if(details == null){
            return Result.error("无数据");
        }
        return Result.success(details);
    }

    @PreAuthorize("@auth.hasCode('aacp:model:remove')")
    @Operation(summary ="删除模型变体")
    @PostMapping("/removeModel")
    public Result<String> removeModel(@RequestBody @Valid CommonIdDto dto) throws Exception{
        modelService.removeModel(dto);
        return Result.success("操作成功");
    }

}
