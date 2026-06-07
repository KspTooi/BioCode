package com.ksptool.bio.biz.qf.controller;

import com.ksptool.assembly.entity.web.Result;
import com.ksptool.bio.biz.qf.commons.LaunchParam;
import com.ksptool.bio.biz.qf.model.qfmodeldeployrcd.dto.LaunchProcDto;
import com.ksptool.bio.biz.qf.model.qftodo.dto.GetProcessApproveFlowDto;
import com.ksptool.bio.biz.qf.model.qftodo.dto.GetProcessApproveFlowRecordDto;
import com.ksptool.bio.biz.qf.model.qftodo.dto.GetProcNodeDefineDto;
import com.ksptool.bio.biz.qf.model.qftodo.vo.ApproveFlowRecordVo;
import com.ksptool.bio.biz.qf.model.qftodo.vo.GetProcNodeDefineVo;
import com.ksptool.bio.biz.qf.service.QfProcService;
import com.ksptool.bio.biz.auth.common.aop.RowScope;
import com.ksptool.bio.biz.auth.common.aop.SystemScope;
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

import java.util.List;


/**
 * QF-流程与任务
 * <p>
 * 这个控制器用于管理流程与任务的启动，暂停，恢复，终止等操作
 * <p>
 *
 * @author Akkarin(1075613357@qq.com)
 * @author (Ish)Yuumi(1144150092@qq.com)
 * @author KspTool(ksptool@outlook.com)
 * @license Proprietary
 * 版权所有 (c) 2026 KspTool及其贡献者保留所有权利。
 * 未经事先书面许可，严禁任何形式的复制或分发。
 * @since 2026-04-17
 */
@PrintLog
@RestController
@RequestMapping("/qfProc")
@Tag(name = "QF-流程与任务", description = "QF-流程与任务")
@Slf4j
@RowScope(mode = RowScope.Mode.ROOT_ONLY)
public class QfProcController {

    @Autowired
    private QfProcService qfProcService;

    @PreAuthorize("@auth.hasCode('qf:proc:launch')")
    @Operation(summary = "发起审批流程(测试用)")
    @PostMapping("/launchProc")
    public Result<String> launchProc(@RequestBody @Valid LaunchProcDto dto) throws Exception {

        var p = LaunchParam.builder()
                .modelCode(dto.getCode())
                .dataId(dto.getDataId())
                .build();
        
        //加入启动成员参数
        for (var member : dto.getMembers()) {
            p.addMember(member.getNodeId(), member.getMemberId());
        }
        
        var processInstanceId = qfProcService.launchProc(p);
        return Result.success(processInstanceId);
    }

    @PreAuthorize("@auth.hasCode('qf:model:deploy:edit')")
    @Operation(summary = "获取流程节点配置列表（发起流程时使用）")
    @PostMapping("/getProcNodeDefine")
    public Result<List<GetProcNodeDefineVo>> getProcNodeDefine(@RequestBody @Valid GetProcNodeDefineDto dto) throws Exception {
        List<GetProcNodeDefineVo> nodes = qfProcService.getProcNodeDefine(dto.getCode());
        if (nodes == null || nodes.isEmpty()) {
            return Result.error("未找到流程节点");
        }
        return Result.success(nodes);
    }

    /**
     * 代办审批的时候回显审批流画布
     *
     */
    @PreAuthorize("@auth.hasCode('qf:todo:details')")
    @Operation(summary = "获取待办事项审批流")
    @PostMapping("/getProcessApproveFlow")
    public Result<String> getQfTodoApproveFlow(@RequestBody @Valid GetProcessApproveFlowDto dto) throws Exception {
        String flow = qfProcService.getProcessApproveFlow(dto);
        if (flow == null) {
            return Result.error("无数据");
        }
        return Result.success(flow);
    }

    /**
     * 代办的流程的流转记录
     * 返回按照时间的顺序
     * 返回 节点名称，节点审批人，节点审批时间，节点审批结果
     *
     */
    @PreAuthorize("@auth.hasCode('qf:todo:details')")
    @Operation(summary = "获取待办事项流程流转记录")
    @PostMapping("/getProcessApproveFlowRecord")
    public Result<List<ApproveFlowRecordVo>> getQfTodoApproveFlowRecord(@RequestBody @Valid GetProcessApproveFlowRecordDto dto) throws Exception {
        List<ApproveFlowRecordVo> records = qfProcService.getProcessApproveFlowRecord(dto);
        if (records == null || records.isEmpty()) {
            return Result.error("无数据");
        }
        return Result.success(records);
    }

}
