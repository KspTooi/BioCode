package com.ksptool.bio.biz.qf.controller;

import com.ksptool.assembly.entity.web.CommonIdDto;
import com.ksptool.assembly.entity.web.PageResult;
import com.ksptool.assembly.entity.web.Result;
import com.ksptool.bio.biz.qf.model.qfmodeldeployrcd.dto.GetQfModelDeployRcdListDto;
import com.ksptool.bio.biz.qf.model.qfmodeldeployrcd.vo.GetQfModelDeployRcdDetailsVo;
import com.ksptool.bio.biz.qf.model.qfmodeldeployrcd.vo.GetQfModelDeployRcdListVo;
import com.ksptool.bio.biz.qf.service.QfModelDeployRcdService;
import com.ksptool.bio.biz.qf.service.QfProcService;
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
 * QF-流程模型部署
 *
 * @author KspTool(ksptool@outlook.com)
 * @license Proprietary
 * 版权所有 (c) 2026 KspTool及其贡献者保留所有权利。
 * 未经事先书面许可，严禁任何形式的复制或分发。
 * @since 2026-04-16
 */
@PrintLog
@RestController
@RequestMapping("/qfModelDeployRcd")
@Tag(name = "QF-流程模型部署", description = "QF-流程模型部署")
@Slf4j
public class QfModelDeployRcdController {

    @Autowired
    private QfModelDeployRcdService qfModelDeployRcdService;

    @Autowired
    private QfProcService qfProcService;

    @PreAuthorize("@auth.hasCode('qf:model:deploy:list')")
    @PostMapping("/getQfModelDeployRcdList")
    @Operation(summary = "查询流程模型部署列表")
    public PageResult<GetQfModelDeployRcdListVo> getQfModelDeployRcdList(@RequestBody @Valid GetQfModelDeployRcdListDto dto) throws Exception {
        return qfModelDeployRcdService.getQfModelDeployRcdList(dto);
    }

    @PreAuthorize("@auth.hasCode('qf:model:deploy:details')")
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


}
