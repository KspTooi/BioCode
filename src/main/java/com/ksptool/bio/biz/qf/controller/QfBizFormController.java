package com.ksptool.bio.biz.qf.controller;

import com.ksptool.assembly.entity.web.CommonIdDto;
import com.ksptool.assembly.entity.web.PageResult;
import com.ksptool.assembly.entity.web.Result;
import com.ksptool.bio.biz.qf.model.qfbizform.dto.AddQfBizFormDto;
import com.ksptool.bio.biz.qf.model.qfbizform.dto.EditQfBizFormDto;
import com.ksptool.bio.biz.qf.model.qfbizform.dto.GetQfBizFormListDto;
import com.ksptool.bio.biz.qf.model.qfbizform.vo.GetQfBizFormDetailsVo;
import com.ksptool.bio.biz.qf.model.qfbizform.vo.GetQfBizFormListVo;
import com.ksptool.bio.biz.qf.service.QfBizFormService;
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
 * QF-业务表单
 * 
 * @author WangQingHua(603484930@qq.com)
 * @author KspTool(ksptool@outlook.com)
 * @since 2026-04-16
 * @license Apache License 2.0
 */
@PrintLog
@RestController
@RequestMapping("/bizForm")
@Tag(name = "QF-业务表单", description = "QF-业务表单")
@Slf4j
public class QfBizFormController {

    @Autowired
    private QfBizFormService qfBizFormService;

    @PreAuthorize("@auth.hasCode('qf:biz:form:view')")
    @PostMapping("/getBizFormList")
    @Operation(summary = "查询业务表单列表")
    public PageResult<GetQfBizFormListVo> getBizFormList(@RequestBody @Valid GetQfBizFormListDto dto) throws Exception {
        return qfBizFormService.getBizFormList(dto);
    }

    @PreAuthorize("@auth.hasCode('qf:biz:form:add')")
    @Operation(summary = "新增业务表单")
    @PostMapping("/addBizForm")
    public Result<String> addBizForm(@RequestBody @Valid AddQfBizFormDto dto) throws Exception {
        qfBizFormService.addBizForm(dto);
        return Result.success("新增成功");
    }

    @PreAuthorize("@auth.hasCode('qf:biz:form:edit')")
    @Operation(summary = "编辑业务表单")
    @PostMapping("/editBizForm")
    public Result<String> editBizForm(@RequestBody @Valid EditQfBizFormDto dto) throws Exception {
        qfBizFormService.editBizForm(dto);
        return Result.success("修改成功");
    }

    @PreAuthorize("@auth.hasCode('qf:biz:form:view')")
    @Operation(summary = "查询业务表单详情")
    @PostMapping("/getBizFormDetails")
    public Result<GetQfBizFormDetailsVo> getBizFormDetails(@RequestBody @Valid CommonIdDto dto) throws Exception {
        GetQfBizFormDetailsVo details = qfBizFormService.getBizFormDetails(dto);
        if (details == null) {
            return Result.error("无数据");
        }
        return Result.success(details);
    }

    @PreAuthorize("@auth.hasCode('qf:biz:form:remove')")
    @Operation(summary = "删除业务表单")
    @PostMapping("/removeBizForm")
    public Result<String> removeBizForm(@RequestBody @Valid CommonIdDto dto) throws Exception {
        qfBizFormService.removeBizForm(dto);
        return Result.success("操作成功");
    }

}
