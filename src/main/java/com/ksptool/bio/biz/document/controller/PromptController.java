package com.ksptool.bio.biz.document.controller;

import com.ksptool.assembly.entity.web.CommonIdDto;
import com.ksptool.assembly.entity.web.PageResult;
import com.ksptool.assembly.entity.web.Result;
import com.ksptool.bio.biz.document.model.prompt.dto.AddPromptDto;
import com.ksptool.bio.biz.document.model.prompt.dto.CompilePromptDto;
import com.ksptool.bio.biz.document.model.prompt.dto.EditPromptDto;
import com.ksptool.bio.biz.document.model.prompt.dto.GetPromptListDto;
import com.ksptool.bio.biz.document.model.prompt.vo.GetPromptDetailsVo;
import com.ksptool.bio.biz.document.model.prompt.vo.GetPromptListVo;
import com.ksptool.bio.biz.document.service.PromptService;
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
@RequestMapping("/prompt")
@Tag(name = "EP-提示词管理", description = "提示词管理")
@Slf4j
public class PromptController {

    @Autowired
    private PromptService promptService;

    @PreAuthorize("@auth.hasCode('ep:prompt:view')")
    @PostMapping("/getPromptList")
    @Operation(summary = "查询提示词列表")
    public PageResult<GetPromptListVo> getPromptList(@RequestBody @Valid GetPromptListDto dto) throws Exception {
        return promptService.getPromptList(dto);
    }

    @PreAuthorize("@auth.hasCode('ep:prompt:add')")
    @Operation(summary = "新增提示词")
    @PostMapping("/addPrompt")
    public Result<String> addPrompt(@RequestBody @Valid AddPromptDto dto) throws Exception {
        promptService.addPrompt(dto);
        return Result.success("新增成功");
    }

    @PreAuthorize("@auth.hasCode('ep:prompt:edit')")
    @Operation(summary = "编辑提示词")
    @PostMapping("/editPrompt")
    public Result<String> editPrompt(@RequestBody @Valid EditPromptDto dto) throws Exception {
        promptService.editPrompt(dto);
        return Result.success("修改成功");
    }

    @PreAuthorize("@auth.hasCode('ep:prompt:view')")
    @Operation(summary = "查询提示词详情")
    @PostMapping("/getPromptDetails")
    public Result<GetPromptDetailsVo> getPromptDetails(@RequestBody @Valid CommonIdDto dto) throws Exception {
        GetPromptDetailsVo details = promptService.getPromptDetails(dto);
        if (details == null) {
            return Result.error("无数据");
        }
        return Result.success(details);
    }

    @PreAuthorize("@auth.hasCode('ep:prompt:remove')")
    @Operation(summary = "删除提示词")
    @PostMapping("/removePrompt")
    public Result<String> removePrompt(@RequestBody @Valid CommonIdDto dto) throws Exception {
        promptService.removePrompt(dto);
        return Result.success("操作成功");
    }

    
    @PreAuthorize("@auth.hasCode('ep:prompt:compile')")
    @Operation(summary = "编译提示词")
    @PostMapping("/compilePrompt")
    public Result<String> compilePrompt(@RequestBody @Valid CompilePromptDto dto) throws Exception {
        var result = promptService.compilePrompt(dto);
        return Result.success(result);
    }

}
