package com.ksptool.bio.biz.qf.controller;

import com.ksptool.assembly.entity.web.CommonIdDto;
import com.ksptool.assembly.entity.web.PageResult;
import com.ksptool.assembly.entity.web.Result;


import com.ksptool.bio.biz.qf.model.qfbizformfield.dto.AddQfBizFormFieldDto;
import com.ksptool.bio.biz.qf.model.qfbizformfield.dto.EditQfBizFormFieldDto;
import com.ksptool.bio.biz.qf.model.qfbizformfield.dto.GetQfBizFormFieldListDto;
import com.ksptool.bio.biz.qf.model.qfbizformfield.vo.GetQfBizFormFieldDetailsVo;
import com.ksptool.bio.biz.qf.model.qfbizformfield.vo.GetQfBizFormFieldListVo;

import com.ksptool.bio.biz.qf.service.QfBizFormFieldService;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import com.ksptool.bio.commons.annotation.PrintLog;



@PrintLog
@RestController
@RequestMapping("/QfBizFormField")
@Tag(name = "流程表单字段配置", description = "流程表单字段配置")
@Slf4j
public class QfBizFormFieldController {

    @Autowired
    private QfBizFormFieldService qfbizFormFieldService;

    @PreAuthorize("@auth.hasCode('qf:biz:form:field:view')")
    @PostMapping("/getQfBizFormFieldList")
    @Operation(summary ="查询流程表单字段配置列表")
    public PageResult<GetQfBizFormFieldListVo> getQfBizFormFieldList(@RequestBody @Valid GetQfBizFormFieldListDto dto) throws Exception{
        return qfbizFormFieldService.getQfBizFormFieldList(dto);
    }

    @PreAuthorize("@auth.hasCode('qf:biz:form:field:add')")
    @Operation(summary ="新增流程表单字段配置")
    @PostMapping("/addQfBizFormField")
    public Result<String> addQfBizFormField(@RequestBody @Valid AddQfBizFormFieldDto dto) throws Exception{
		qfbizFormFieldService.addQfBizFormField(dto);
        return Result.success("新增成功");
    }

    @PreAuthorize("@auth.hasCode('qf:biz:form:field:edit')")
    @Operation(summary ="编辑流程表单字段配置")
    @PostMapping("/editQfBizFormField")
    public Result<String> editQfBizFormField(@RequestBody @Valid EditQfBizFormFieldDto dto) throws Exception{
		qfbizFormFieldService.editQfBizFormField(dto);
        return Result.success("修改成功");
    }

    @PreAuthorize("@auth.hasCode('qf:biz:form:field:view')")
    @Operation(summary ="查询流程表单字段配置详情")
    @PostMapping("/getQfBizFormFieldDetails")
    public Result<GetQfBizFormFieldDetailsVo> getQfBizFormFieldDetails(@RequestBody @Valid CommonIdDto dto) throws Exception{
        GetQfBizFormFieldDetailsVo details = qfbizFormFieldService.getQfBizFormFieldDetails(dto);
        if(details == null){
            return Result.error("无数据");
        }
        return Result.success(details);
    }

    @PreAuthorize("@auth.hasCode('qf:biz:form:field:remove')")
    @Operation(summary ="删除流程表单字段配置")
    @PostMapping("/removeQfBizFormField")
    public Result<String> removeQfBizFormField(@RequestBody @Valid CommonIdDto dto) throws Exception{
        qfbizFormFieldService.removeQfBizFormField(dto);
        return Result.success("操作成功");
    }

}
