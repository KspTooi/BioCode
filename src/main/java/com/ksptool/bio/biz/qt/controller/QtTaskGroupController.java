package com.ksptool.bio.biz.qt.controller;

import com.ksptool.bio.biz.qt.model.qttaskgroup.dto.AddQtTaskGroupDto;
import com.ksptool.bio.biz.qt.model.qttaskgroup.dto.EditQtTaskGroupDto;
import com.ksptool.bio.biz.qt.model.qttaskgroup.dto.GetQtTaskGroupListDto;
import com.ksptool.bio.biz.qt.model.qttaskgroup.vo.GetQtTaskGroupDetailsVo;
import com.ksptool.bio.biz.qt.model.qttaskgroup.vo.GetQtTaskGroupListVo;
import com.ksptool.bio.biz.qt.service.QtTaskGroupService;
import com.ksptool.assembly.entity.web.CommonIdDto;
import com.ksptool.assembly.entity.web.PageResult;
import com.ksptool.assembly.entity.web.Result;
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
 * 任务分组
 * 
 * @author KspTool(ksptool@outlook.com)
 * @since 2026-02-10
 * @license Apache License 2.0
 */
@PrintLog
@RestController
@RequestMapping("/qtTaskGroup")
@Tag(name = "任务分组", description = "任务分组模块")
@Slf4j
public class QtTaskGroupController {

    @Autowired
    private QtTaskGroupService qtTaskGroupService;

    @PreAuthorize("@auth.hasCode('qt:group:view')")
    @PostMapping("/getQtTaskGroupList")
    @Operation(summary = "查询任务分组列表")
    public PageResult<GetQtTaskGroupListVo> getQtTaskGroupList(@RequestBody @Valid GetQtTaskGroupListDto dto) throws Exception {
        return qtTaskGroupService.getQtTaskGroupList(dto);
    }

    @PreAuthorize("@auth.hasCode('qt:group:add')")
    @Operation(summary = "新增任务分组")
    @PostMapping("/addQtTaskGroup")
    public Result<String> addQtTaskGroup(@RequestBody @Valid AddQtTaskGroupDto dto) throws Exception {
        qtTaskGroupService.addQtTaskGroup(dto);
        return Result.success("新增成功");
    }

    @PreAuthorize("@auth.hasCode('qt:group:edit')")
    @Operation(summary = "编辑任务分组")
    @PostMapping("/editQtTaskGroup")
    public Result<String> editQtTaskGroup(@RequestBody @Valid EditQtTaskGroupDto dto) throws Exception {
        qtTaskGroupService.editQtTaskGroup(dto);
        return Result.success("修改成功");
    }

    @PreAuthorize("@auth.hasCode('qt:group:view')")
    @Operation(summary = "查询任务分组详情")
    @PostMapping("/getQtTaskGroupDetails")
    public Result<GetQtTaskGroupDetailsVo> getQtTaskGroupDetails(@RequestBody @Valid CommonIdDto dto) throws Exception {
        GetQtTaskGroupDetailsVo details = qtTaskGroupService.getQtTaskGroupDetails(dto);
        if (details == null) {
            return Result.error("无数据");
        }
        return Result.success(details);
    }

    @PreAuthorize("@auth.hasCode('qt:group:remove')")
    @Operation(summary = "删除任务分组")
    @PostMapping("/removeQtTaskGroup")
    public Result<String> removeQtTaskGroup(@RequestBody @Valid CommonIdDto dto) throws Exception {
        qtTaskGroupService.removeQtTaskGroup(dto);
        return Result.success("操作成功");
    }

}
