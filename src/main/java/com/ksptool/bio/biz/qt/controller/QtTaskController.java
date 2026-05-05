package com.ksptool.bio.biz.qt.controller;

import com.ksptool.assembly.entity.web.CommonIdDto;
import com.ksptool.assembly.entity.web.PageResult;
import com.ksptool.assembly.entity.web.Result;
import com.ksptool.bio.biz.qt.model.qttask.dto.*;
import com.ksptool.bio.biz.qt.model.qttask.vo.GetLocalBeanListVo;
import com.ksptool.bio.biz.qt.model.qttask.vo.GetQtTaskDetailsVo;
import com.ksptool.bio.biz.qt.model.qttask.vo.GetQtTaskListVo;
import com.ksptool.bio.biz.qt.service.QtTaskService;
import com.ksptool.bio.commons.annotation.PrintLog;
import com.ksptool.bio.commons.dataprocess.ExportWizard;
import com.ksptool.bio.commons.dataprocess.ImportWizard;
import com.ksptool.bio.commons.dataprocess.Str;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 任务调度
 *
 * @author KspTool(ksptool@outlook.com)
 * @license Apache License 2.0
 * @since 1.6.1(A).1
 */
@PrintLog
@RestController
@RequestMapping("/qtTask")
@Tag(name = "QT-任务调度", description = "任务调度模块")
@Slf4j
public class QtTaskController {

    @Autowired
    private QtTaskService qtTaskService;

    @PreAuthorize("@auth.hasCode('qt:task:list')")
    @PostMapping("/getQtTaskList")
    @Operation(summary = "获取任务列表")
    public PageResult<GetQtTaskListVo> getQtTaskList(@RequestBody @Valid GetQtTaskListDto dto) throws Exception {

        var vos = qtTaskService.getQtTaskList(dto);
        var now = LocalDateTime.now();

        for (GetQtTaskListVo vo : vos.getData()) {
            var expireTime = vo.getExpireTime();
            vo.setIsExpired(0);
            if (expireTime != null && expireTime.isBefore(now)) {
                vo.setIsExpired(1);
            }
        }

        return vos;
    }

    @PreAuthorize("@auth.hasCode('qt:task:add')")
    @Operation(summary = "新增任务")
    @PostMapping("/addQtTask")
    public Result<String> addQtTask(@RequestBody @Valid AddQtTaskDto dto) throws Exception {

        //暂不支持HTTP模式
        if (dto.getKind() == 1) {
            return Result.error("暂不支持HTTP模式");
        }

        //验证参数
        String validate = dto.validate();
        if (Str.isNotBlank(validate)) {
            return Result.error(validate);
        }

        qtTaskService.addQtTask(dto);
        return Result.success("新增成功");
    }

    @PreAuthorize("@auth.hasCode('qt:task:edit')")
    @Operation(summary = "编辑任务")
    @PostMapping("/editQtTask")
    public Result<String> editQtTask(@RequestBody @Valid EditQtTaskDto dto) throws Exception {

        //暂不支持HTTP模式
        if (dto.getKind() == 1) {
            return Result.error("暂不支持HTTP模式");
        }

        //验证参数
        String validate = dto.validate();
        if (Str.isNotBlank(validate)) {
            return Result.error(validate);
        }

        qtTaskService.editQtTask(dto);
        return Result.success("修改成功");
    }

    @PreAuthorize("@auth.hasCode('qt:task:details')")
    @Operation(summary = "获取任务详情")
    @PostMapping("/getQtTaskDetails")
    public Result<GetQtTaskDetailsVo> getQtTaskDetails(@RequestBody @Valid CommonIdDto dto) throws Exception {
        GetQtTaskDetailsVo details = qtTaskService.getQtTaskDetails(dto);
        if (details == null) {
            return Result.error("无数据");
        }
        return Result.success(details);
    }

    @PreAuthorize("@auth.hasCode('qt:task:remove')")
    @Operation(summary = "删除任务")
    @PostMapping("/removeQtTask")
    public Result<String> removeQtTask(@RequestBody @Valid CommonIdDto dto) throws Exception {
        qtTaskService.removeQtTask(dto);
        return Result.success("操作成功");
    }

    @PreAuthorize("@auth.hasCode('qt:task:edit')")
    @Operation(summary = "获取本地任务Bean列表")
    @PostMapping("/getLocalBeanList")
    public Result<List<GetLocalBeanListVo>> getLocalBeanList() throws Exception {
        return Result.success(qtTaskService.getLocalBeanList());
    }

    @PreAuthorize("@auth.hasCode('qt:task:edit')")
    @Operation(summary = "立即执行任务")
    @PostMapping("/executeTask")
    public Result<String> executeTask(@RequestBody @Valid ExecuteTaskDto dto) throws Exception {
        qtTaskService.executeTask(dto);
        return Result.success("操作成功");
    }


    @PreAuthorize("@auth.hasCode('qt:task:import')")
    @Operation(summary = "导入任务")
    @PostMapping("/importQtTask")
    public Result<String> importQtTask(@RequestParam("file") MultipartFile file) throws Exception {

        //准备向导
        var iw = new ImportWizard<>(file, ImportQtTaskDto.class);

        //开始传输
        iw.transfer();

        //验证导入数据
        var errors = iw.validate();
        if (Str.isNotBlank(errors)) {
            return Result.error(errors);
        }

        //获取导入数据
        var data = iw.getData();
        var success = qtTaskService.importQtTask(data);
        return Result.success("操作成功,已导入数据:" + success + "条", null);
    }

    @PreAuthorize("@auth.hasCode('qt:task:export')")
    @Operation(summary = "导出任务")
    @RequestMapping("/exportQtTask")
    public void exportQtTask(@RequestBody @Valid GetQtTaskListDto dto, HttpServletResponse hsrp) throws Exception {
        //准备导出向导
        var data = qtTaskService.exportQtTask(dto);

        //需要处理一下数据 将终止的任务改为暂停
        for (var vo : data) {
            if (vo.getStatus() == 2) {
                vo.setStatus(1);
            }
        }
        var ew = new ExportWizard<>(data, hsrp);
        ew.transfer("任务调度");
    }

}
