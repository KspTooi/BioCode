package com.ksptool.bio.biz.polytemplatefield.controller;

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

import com.ksptool.bio.biz.polytemplatefield.service.PolyTemplateFieldService;
import com.ksptool.bio.biz.polytemplatefield.model.dto.AddPolyTemplateFieldDto;
import com.ksptool.bio.biz.polytemplatefield.model.dto.EditPolyTemplateFieldDto;
import com.ksptool.bio.biz.polytemplatefield.model.dto.GetPolyTemplateFieldListDto;
import com.ksptool.bio.biz.polytemplatefield.model.vo.GetPolyTemplateFieldListVo;
import com.ksptool.bio.biz.polytemplatefield.model.vo.GetPolyTemplateFieldDetailsVo;

@PrintLog
@RestController
@RequestMapping("/polyTemplateField")
@Tag(name = "聚合模板字段", description = "聚合模板字段")
@Slf4j
public class PolyTemplateFieldController {

    @Autowired
    private PolyTemplateFieldService polyTemplateFieldService;

    @PreAuthorize("@auth.hasCode('assembly:poly:template:field:view')")
    @PostMapping("/getPolyTemplateFieldList")
    @Operation(summary ="查询聚合模板字段列表")
    public PageResult<GetPolyTemplateFieldListVo> getPolyTemplateFieldList(@RequestBody @Valid GetPolyTemplateFieldListDto dto) throws Exception{
        return polyTemplateFieldService.getPolyTemplateFieldList(dto);
    }

    @PreAuthorize("@auth.hasCode('assembly:poly:template:field:add')")
    @Operation(summary ="新增聚合模板字段")
    @PostMapping("/addPolyTemplateField")
    public Result<String> addPolyTemplateField(@RequestBody @Valid AddPolyTemplateFieldDto dto) throws Exception{
		polyTemplateFieldService.addPolyTemplateField(dto);
        return Result.success("新增成功");
    }

    @PreAuthorize("@auth.hasCode('assembly:poly:template:field:edit')")
    @Operation(summary ="编辑聚合模板字段")
    @PostMapping("/editPolyTemplateField")
    public Result<String> editPolyTemplateField(@RequestBody @Valid EditPolyTemplateFieldDto dto) throws Exception{
		polyTemplateFieldService.editPolyTemplateField(dto);
        return Result.success("修改成功");
    }

    @PreAuthorize("@auth.hasCode('assembly:poly:template:field:view')")
    @Operation(summary ="查询聚合模板字段详情")
    @PostMapping("/getPolyTemplateFieldDetails")
    public Result<GetPolyTemplateFieldDetailsVo> getPolyTemplateFieldDetails(@RequestBody @Valid CommonIdDto dto) throws Exception{
        GetPolyTemplateFieldDetailsVo details = polyTemplateFieldService.getPolyTemplateFieldDetails(dto);
        if(details == null){
            return Result.error("无数据");
        }
        return Result.success(details);
    }

    @PreAuthorize("@auth.hasCode('assembly:poly:template:field:remove')")
    @Operation(summary ="删除聚合模板字段")
    @PostMapping("/removePolyTemplateField")
    public Result<String> removePolyTemplateField(@RequestBody @Valid CommonIdDto dto) throws Exception{
        polyTemplateFieldService.removePolyTemplateField(dto);
        return Result.success("操作成功");
    }

}
