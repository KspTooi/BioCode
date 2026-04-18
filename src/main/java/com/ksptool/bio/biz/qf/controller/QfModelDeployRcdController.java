package com.ksptool.bio.biz.qf.controller;

import com.ksptool.assembly.entity.web.CommonIdDto;
import com.ksptool.assembly.entity.web.PageResult;
import com.ksptool.assembly.entity.web.Result;
import com.ksptool.bio.biz.qf.model.qfmodeldeployrcd.dto.GetQfModelDeployRcdListDto;
import com.ksptool.bio.biz.qf.model.qfmodeldeployrcd.dto.LaunchQfProcessDto;
import com.ksptool.bio.biz.qf.model.qfmodeldeployrcd.vo.GetQfModelDeployRcdDetailsVo;
import com.ksptool.bio.biz.qf.model.qfmodeldeployrcd.vo.GetQfModelDeployRcdListVo;
import com.ksptool.bio.biz.qf.service.QfModelDeployRcdService;
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
@RequestMapping("/qfModelDeployRcd")
@Tag(name = "流程模型部署", description = "流程模型部署")
@Slf4j
public class QfModelDeployRcdController {

    @Autowired
    private QfModelDeployRcdService qfModelDeployRcdService;

    @PreAuthorize("@auth.hasCode('qf:model:deploy:view')")
    @PostMapping("/getQfModelDeployRcdList")
    @Operation(summary = "查询流程模型部署列表")
    public PageResult<GetQfModelDeployRcdListVo> getQfModelDeployRcdList(@RequestBody @Valid GetQfModelDeployRcdListDto dto) throws Exception {
        return qfModelDeployRcdService.getQfModelDeployRcdList(dto);
    }

    @PreAuthorize("@auth.hasCode('qf:model:deploy:view')")
    @Operation(summary = "查询流程模型部署详情")
    @PostMapping("/getQfModelDeployRcdDetails")
    public Result<GetQfModelDeployRcdDetailsVo> getQfModelDeployRcdDetails(@RequestBody @Valid CommonIdDto dto) throws Exception {
        GetQfModelDeployRcdDetailsVo details = qfModelDeployRcdService.getQfModelDeployRcdDetails(dto);
        if (details == null) {
            return Result.error("无数据");
        }
        return Result.success(details);
    }

    @PreAuthorize("@auth.hasCode('qf:model:deploy:remove')")
    @Operation(summary = "删除流程模型部署")
    @PostMapping("/removeQfModelDeployRcd")
    public Result<String> removeQfModelDeployRcd(@RequestBody @Valid CommonIdDto dto) throws Exception {
        qfModelDeployRcdService.removeQfModelDeployRcd(dto);
        return Result.success("操作成功");
    }

    @PreAuthorize("@auth.hasCode('qf:model:deploy:edit')")
    @Operation(summary = "挂起流程模型部署")
    @PostMapping("/suspendQfModelDeployRcd")
    public Result<String> suspendQfModelDeployRcd(@RequestBody @Valid CommonIdDto dto) throws Exception {
        qfModelDeployRcdService.suspendQfModelDeployRcd(dto);
        return Result.success("操作成功");
    }

    @PreAuthorize("@auth.hasCode('qf:model:deploy:edit')")
    @Operation(summary = "激活流程模型部署")
    @PostMapping("/activateQfModelDeployRcd")
    public Result<String> activateQfModelDeployRcd(@RequestBody @Valid CommonIdDto dto) throws Exception {
        qfModelDeployRcdService.activateQfModelDeployRcd(dto);
        return Result.success("操作成功");
    }

    @PreAuthorize("@auth.hasCode('qf:model:deploy:edit')")
    @Operation(summary = "发起审批流程(测试)")
    @PostMapping("/launchQfProcess")
    public Result<String> launchQfProcess(@RequestBody @Valid LaunchQfProcessDto dto) throws Exception {
        String processInstanceId = qfModelDeployRcdService.launchQfProcess(dto);
        return Result.success(processInstanceId);
    }

}
