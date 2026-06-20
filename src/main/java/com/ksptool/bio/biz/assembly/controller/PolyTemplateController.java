package com.ksptool.bio.biz.assembly.controller;

import com.ksptool.assembly.entity.web.CommonIdDto;
import com.ksptool.assembly.entity.web.PageResult;
import com.ksptool.assembly.entity.web.Result;
import com.ksptool.bio.biz.assembly.model.polytemplate.dto.AddPolyTemplateDto;
import com.ksptool.bio.biz.assembly.model.polytemplate.dto.EditPolyTemplateDto;
import com.ksptool.bio.biz.assembly.model.polytemplate.dto.GetPolyTemplateListDto;
import com.ksptool.bio.biz.assembly.model.polytemplate.vo.GetPolyTemplateDetailsVo;
import com.ksptool.bio.biz.assembly.model.polytemplate.vo.GetPolyTemplateListVo;
import com.ksptool.bio.biz.assembly.service.PolyTemplateService;
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

/**
 * @author KspTooi
 * @since 1.7.9(I).1
 */
@PrintLog
@RestController
@RequestMapping("/polyTemplate")
@Tag(name = "聚合模板", description = "聚合模板")
@Slf4j
public class PolyTemplateController {

    @Autowired
    private PolyTemplateService polyTemplateService;

    @PreAuthorize("@auth.hasCode('assembly:poly:template:view')")
    @PostMapping("/getPolyTemplateList")
    @Operation(summary = "查询聚合模板列表")
    public PageResult<GetPolyTemplateListVo> getPolyTemplateList(@RequestBody @Valid GetPolyTemplateListDto dto) throws Exception {
        return polyTemplateService.getPolyTemplateList(dto);
    }

    @PreAuthorize("@auth.hasCode('assembly:poly:template:add')")
    @Operation(summary = "新增聚合模板")
    @PostMapping("/addPolyTemplate")
    public Result<String> addPolyTemplate(@RequestBody @Valid AddPolyTemplateDto dto) throws Exception {
        polyTemplateService.addPolyTemplate(dto);
        return Result.success("新增成功");
    }

    @PreAuthorize("@auth.hasCode('assembly:poly:template:edit')")
    @Operation(summary = "编辑聚合模板")
    @PostMapping("/editPolyTemplate")
    public Result<String> editPolyTemplate(@RequestBody @Valid EditPolyTemplateDto dto) throws Exception {
        polyTemplateService.editPolyTemplate(dto);
        return Result.success("修改成功");
    }

    @PreAuthorize("@auth.hasCode('assembly:poly:template:view')")
    @Operation(summary = "查询聚合模板详情")
    @PostMapping("/getPolyTemplateDetails")
    public Result<GetPolyTemplateDetailsVo> getPolyTemplateDetails(@RequestBody @Valid CommonIdDto dto) throws Exception {
        GetPolyTemplateDetailsVo details = polyTemplateService.getPolyTemplateDetails(dto);
        if (details == null) {
            return Result.error("无数据");
        }
        return Result.success(details);
    }

    @PreAuthorize("@auth.hasCode('assembly:poly:template:remove')")
    @Operation(summary = "删除聚合模板")
    @PostMapping("/removePolyTemplate")
    public Result<String> removePolyTemplate(@RequestBody @Valid CommonIdDto dto) throws Exception {
        polyTemplateService.removePolyTemplate(dto);
        return Result.success("操作成功");
    }

}
