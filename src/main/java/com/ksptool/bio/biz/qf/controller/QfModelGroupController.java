package com.ksptool.bio.biz.qf.controller;

import com.ksptool.assembly.entity.web.CommonIdDto;
import com.ksptool.assembly.entity.web.PageResult;
import com.ksptool.assembly.entity.web.Result;
import com.ksptool.bio.biz.qf.model.qfmodelgroup.dto.AddQfModelGroupDto;
import com.ksptool.bio.biz.qf.model.qfmodelgroup.dto.EditQfModelGroupDto;
import com.ksptool.bio.biz.qf.model.qfmodelgroup.dto.GetQfModelGroupListDto;
import com.ksptool.bio.biz.qf.model.qfmodelgroup.vo.GetQfModelGroupDetailsVo;
import com.ksptool.bio.biz.qf.model.qfmodelgroup.vo.GetQfModelGroupListVo;
import com.ksptool.bio.biz.qf.service.QfModelGroupService;
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
 * QF-流程模型分组
 *
 * @author KspTool(ksptool@outlook.com)
 * @license Proprietary
 * 版权所有 (c) 2026 KspTool及其贡献者保留所有权利。
 * 未经事先书面许可，严禁任何形式的复制或分发。
 * @since 2026-04-16
 */
@PrintLog
@RestController
@RequestMapping("/qfModelGroup")
@Tag(name = "QF-流程模型分组", description = "QF-流程模型分组")
@Slf4j
public class QfModelGroupController {

    @Autowired
    private QfModelGroupService qfModelGroupService;

    @PreAuthorize("@auth.hasCode('qf:model:group:list')")
    @PostMapping("/getQfModelGroupList")
    @Operation(summary = "查询流程模型分组列表")
    public PageResult<GetQfModelGroupListVo> getQfModelGroupList(@RequestBody @Valid GetQfModelGroupListDto dto) throws Exception {
        return qfModelGroupService.getQfModelGroupList(dto);
    }

    @PreAuthorize("@auth.hasCode('qf:model:group:add')")
    @Operation(summary = "新增流程模型分组")
    @PostMapping("/addQfModelGroup")
    public Result<String> addQfModelGroup(@RequestBody @Valid AddQfModelGroupDto dto) throws Exception {
        qfModelGroupService.addQfModelGroup(dto);
        return Result.success("新增成功");
    }

    @PreAuthorize("@auth.hasCode('qf:model:group:edit')")
    @Operation(summary = "编辑流程模型分组")
    @PostMapping("/editQfModelGroup")
    public Result<String> editQfModelGroup(@RequestBody @Valid EditQfModelGroupDto dto) throws Exception {
        qfModelGroupService.editQfModelGroup(dto);
        return Result.success("修改成功");
    }

    @PreAuthorize("@auth.hasCode('qf:model:group:details')")
    @Operation(summary = "查询流程模型分组详情")
    @PostMapping("/getQfModelGroupDetails")
    public Result<GetQfModelGroupDetailsVo> getQfModelGroupDetails(@RequestBody @Valid CommonIdDto dto) throws Exception {
        GetQfModelGroupDetailsVo details = qfModelGroupService.getQfModelGroupDetails(dto);
        if (details == null) {
            return Result.error("无数据");
        }
        return Result.success(details);
    }

    @PreAuthorize("@auth.hasCode('qf:model:group:remove')")
    @Operation(summary = "删除流程模型分组")
    @PostMapping("/removeQfModelGroup")
    public Result<String> removeQfModelGroup(@RequestBody @Valid CommonIdDto dto) throws Exception {

        //不支持批量删除
        if (dto.isBatch()) {
            return Result.error("不支持批量删除");
        }

        qfModelGroupService.removeQfModelGroup(dto);
        return Result.success("操作成功");
    }

}
