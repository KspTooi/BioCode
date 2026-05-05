package com.ksptool.bio.biz.core.controller;

import com.ksptool.bio.biz.core.model.noticetemplate.dto.AddNoticeTemplateDto;
import com.ksptool.bio.biz.core.model.noticetemplate.dto.EditNoticeTemplateDto;
import com.ksptool.bio.biz.core.model.noticetemplate.dto.GetNoticeTemplateListDto;
import com.ksptool.bio.biz.core.model.noticetemplate.vo.GetNoticeTemplateDetailsVo;
import com.ksptool.bio.biz.core.model.noticetemplate.vo.GetNoticeTemplateListVo;
import com.ksptool.bio.biz.core.service.NoticeTemplateService;
import com.ksptool.assembly.entity.web.CommonIdDto;
import com.ksptool.assembly.entity.web.PageResult;
import com.ksptool.assembly.entity.web.Result;
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


@RestController
@RequestMapping("/noticeTemplate")
@Tag(name = "CORE-消息通知模板管理", description = "消息通知模板管理")
@Slf4j
public class NoticeTemplateController {

    @Autowired
    private NoticeTemplateService noticeTemplateService;

    @PreAuthorize("@auth.hasCode('core:notice_template:list')")
    @PostMapping("/getNoticeTemplateList")
    @Operation(summary = "查询通知模板列表")
    public PageResult<GetNoticeTemplateListVo> getNoticeTemplateList(@RequestBody @Valid GetNoticeTemplateListDto dto) throws Exception {
        return noticeTemplateService.getNoticeTemplateList(dto);
    }

    @PreAuthorize("@auth.hasCode('core:notice_template:add')")
    @Operation(summary = "新增通知模板")
    @PostMapping("/addNoticeTemplate")
    public Result<String> addNoticeTemplate(@RequestBody @Valid AddNoticeTemplateDto dto) throws Exception {
        noticeTemplateService.addNoticeTemplate(dto);
        return Result.success("新增成功");
    }

    @PreAuthorize("@auth.hasCode('core:notice_template:edit')")
    @Operation(summary = "编辑通知模板")
    @PostMapping("/editNoticeTemplate")
    public Result<String> editNoticeTemplate(@RequestBody @Valid EditNoticeTemplateDto dto) throws Exception {
        noticeTemplateService.editNoticeTemplate(dto);
        return Result.success("修改成功");
    }

    @PreAuthorize("@auth.hasCode('core:notice_template:details')")
    @Operation(summary = "查询通知模板详情")
    @PostMapping("/getNoticeTemplateDetails")
    public Result<GetNoticeTemplateDetailsVo> getNoticeTemplateDetails(@RequestBody @Valid CommonIdDto dto) throws Exception {
        GetNoticeTemplateDetailsVo details = noticeTemplateService.getNoticeTemplateDetails(dto);
        if (details == null) {
            return Result.error("无数据");
        }
        return Result.success(details);
    }

    @PreAuthorize("@auth.hasCode('core:notice_template:remove')")
    @Operation(summary = "删除通知模板")
    @PostMapping("/removeNoticeTemplate")
    public Result<String> removeNoticeTemplate(@RequestBody @Valid CommonIdDto dto) throws Exception {
        noticeTemplateService.removeNoticeTemplate(dto);
        return Result.success("操作成功");
    }

}
