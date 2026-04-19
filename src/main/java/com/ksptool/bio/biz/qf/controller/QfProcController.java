package com.ksptool.bio.biz.qf.controller;

import com.ksptool.assembly.entity.web.Result;
import com.ksptool.bio.biz.qf.model.qfmodeldeployrcd.dto.LaunchQfProcessDto;
import com.ksptool.bio.biz.qf.service.QfProcManager;
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
 * QF-流程与任务
 * <p>
 * 这个控制器用于管理流程与任务的启动，暂停，恢复，终止等操作
 * <p>
 *
 * @author Akkarin(1075613357@qq.com)
 * @author (Ish)Yuumi(1144150092@qq.com)
 * @author KspTool(ksptool@outlook.com)
 * @since 2026-04-17
 */
@PrintLog
@RestController
@RequestMapping("/qfProc")
@Tag(name = "QF-流程与任务", description = "QF-流程与任务")
@Slf4j
public class QfProcController {

    @Autowired
    private QfProcManager qfProcManager;


    @PreAuthorize("@auth.hasCode('qf:model:deploy:edit')")
    @Operation(summary = "发起审批流程")
    @PostMapping("/launchQfProcess")
    public Result<String> launchQfProcess(@RequestBody @Valid LaunchQfProcessDto dto) throws Exception {
        String processInstanceId = qfProcManager.launchProc(dto.getCode(), dto.getBizFormCode(), dto.getDataId());
        return Result.success(processInstanceId);
    }

}
