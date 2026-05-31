package com.ksptool.bio.biz.qf.controller;

import com.ksptool.assembly.entity.web.CommonIdDto;
import com.ksptool.assembly.entity.web.PageResult;
import com.ksptool.assembly.entity.web.Result;
import com.ksptool.bio.biz.qf.commons.qfe.QfeBpmnModel;
import com.ksptool.bio.biz.qf.model.qfmodel.dto.AddQfModelDto;
import com.ksptool.bio.biz.qf.model.qfmodel.dto.DesignQfModelDto;
import com.ksptool.bio.biz.qf.model.qfmodel.dto.EditQfModelDto;
import com.ksptool.bio.biz.qf.model.qfmodel.dto.GetQfModelListDto;
import com.ksptool.bio.biz.qf.model.qfmodel.vo.GetQfModelDetailsVo;
import com.ksptool.bio.biz.qf.model.qfmodel.vo.GetQfModelListVo;
import com.ksptool.bio.biz.qf.service.QfModelService;
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
 * QF-流程模型
 *
 * @author KspTool(ksptool@outlook.com)
 * @license Proprietary
 * 版权所有 (c) 2026 KspTool及其贡献者保留所有权利。
 * 未经事先书面许可，严禁任何形式的复制或分发。
 * @since 2026-04-16
 */
@PrintLog
@RestController
@RequestMapping("/qfModel")
@Tag(name = "QF-流程模型", description = "QF-流程模型")
@Slf4j
public class QfModelController {

    @Autowired
    private QfModelService qfModelService;

    @PreAuthorize("@auth.hasCode('qf:model:list')")
    @PostMapping("/getQfModelList")
    @Operation(summary = "查询流程模型列表")
    public PageResult<GetQfModelListVo> getQfModelList(@RequestBody @Valid GetQfModelListDto dto) throws Exception {
        return qfModelService.getQfModelList(dto);
    }

    @PreAuthorize("@auth.hasCode('qf:model:add')")
    @Operation(summary = "新增流程模型")
    @PostMapping("/addQfModel")
    public Result<String> addQfModel(@RequestBody @Valid AddQfModelDto dto) throws Exception {
        qfModelService.addQfModel(dto);
        return Result.success("新增成功");
    }

    @PreAuthorize("@auth.hasCode('qf:model:add')")
    @Operation(summary = "创建新版本流程模型")
    @PostMapping("/createNewVersion")
    public Result<String> createNewVersion(@RequestBody @Valid CommonIdDto dto) throws Exception {
        qfModelService.createNewVersionQfModel(dto);
        return Result.success("创建新版本成功");
    }

    @PreAuthorize("@auth.hasCode('qf:model:edit')")
    @Operation(summary = "编辑流程模型(基础信息)")
    @PostMapping("/editQfModel")
    public Result<String> editQfModel(@RequestBody @Valid EditQfModelDto dto) throws Exception {
        qfModelService.editQfModel(dto);
        return Result.success("修改成功");
    }

    @PreAuthorize("@auth.hasCode('qf:model:edit')")
    @Operation(summary = "设计流程模型BPMN")
    @PostMapping("/designQfModel")
    public Result<String> designQfModel(@RequestBody @Valid DesignQfModelDto dto) throws Exception {
        qfModelService.designQfModel(dto);
        return Result.success("修改成功");
    }


    @PreAuthorize("@auth.hasCode('qf:model:details')")
    @Operation(summary = "查询流程模型详情")
    @PostMapping("/getQfModelDetails")
    public Result<GetQfModelDetailsVo> getQfModelDetails(@RequestBody @Valid CommonIdDto dto) throws Exception {
        GetQfModelDetailsVo details = qfModelService.getQfModelDetails(dto);
        if (details == null) {
            return Result.error("无数据");
        }
        return Result.success(details);
    }

    @PreAuthorize("@auth.hasCode('qf:model:remove')")
    @Operation(summary = "删除流程模型")
    @PostMapping("/removeQfModel")
    public Result<String> removeQfModel(@RequestBody @Valid CommonIdDto dto) throws Exception {

        //不支持批量删除
        if (dto.isBatch()) {
            return Result.error("不支持批量删除");
        }

        qfModelService.removeQfModel(dto);
        return Result.success("操作成功");
    }

    @PreAuthorize("@auth.hasCode('qf:model:deploy')")
    @Operation(summary = "部署流程模型")
    @PostMapping("/deployQfModel")
    public Result<String> deployQfModel(@RequestBody @Valid CommonIdDto dto) throws Exception {
        qfModelService.deployQfModel(dto);
        return Result.success("操作成功");
    }

}
