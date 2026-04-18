package com.ksptool.bio.biz.qf.controller;

import com.ksptool.assembly.entity.web.CommonIdDto;
import com.ksptool.assembly.entity.web.PageResult;
import com.ksptool.assembly.entity.web.Result;
import com.ksptool.bio.biz.qf.model.qfbizform.dto.AddQfBizFormDto;
import com.ksptool.bio.biz.qf.model.qfbizform.dto.EditQfBizFormDto;
import com.ksptool.bio.biz.qf.model.qfbizform.dto.GetQfBizFormListDto;
import com.ksptool.bio.biz.qf.model.qfbizform.vo.GetQfBizFormDetailsVo;
import com.ksptool.bio.biz.qf.model.qfbizform.vo.GetQfBizFormListVo;
import com.ksptool.bio.biz.qf.service.BizFormService;
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

@PrintLog
@RestController
@RequestMapping("/bizForm")
@Tag(name = "业务表单", description = "业务表单")
@Slf4j
public class QfBizFormController {

    @Autowired
    private BizFormService bizFormService;

    @PreAuthorize("@auth.hasCode('qf:biz:form:view')")
    @PostMapping("/getBizFormList")
    @Operation(summary = "查询业务表单列表")
    public PageResult<GetQfBizFormListVo> getBizFormList(@RequestBody @Valid GetQfBizFormListDto dto) throws Exception {
        return bizFormService.getBizFormList(dto);
    }

    @PreAuthorize("@auth.hasCode('qf:biz:form:add')")
    @Operation(summary = "新增业务表单")
    @PostMapping("/addBizForm")
    public Result<String> addBizForm(@RequestBody @Valid AddQfBizFormDto dto) throws Exception {
        bizFormService.addBizForm(dto);
        return Result.success("新增成功");
    }

    @PreAuthorize("@auth.hasCode('qf:biz:form:edit')")
    @Operation(summary = "编辑业务表单")
    @PostMapping("/editBizForm")
    public Result<String> editBizForm(@RequestBody @Valid EditQfBizFormDto dto) throws Exception {
        bizFormService.editBizForm(dto);
        return Result.success("修改成功");
    }

    @PreAuthorize("@auth.hasCode('qf:biz:form:view')")
    @Operation(summary = "查询业务表单详情")
    @PostMapping("/getBizFormDetails")
    public Result<GetQfBizFormDetailsVo> getBizFormDetails(@RequestBody @Valid CommonIdDto dto) throws Exception {
        GetQfBizFormDetailsVo details = bizFormService.getBizFormDetails(dto);
        if (details == null) {
            return Result.error("无数据");
        }
        return Result.success(details);
    }

    @PreAuthorize("@auth.hasCode('qf:biz:form:remove')")
    @Operation(summary = "删除业务表单")
    @PostMapping("/removeBizForm")
    public Result<String> removeBizForm(@RequestBody @Valid CommonIdDto dto) throws Exception {
        bizFormService.removeBizForm(dto);
        return Result.success("操作成功");
    }

}
