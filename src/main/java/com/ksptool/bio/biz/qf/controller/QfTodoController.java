package com.ksptool.bio.biz.qf.controller;

import com.ksptool.assembly.entity.web.CommonIdDto;
import com.ksptool.assembly.entity.web.PageResult;
import com.ksptool.assembly.entity.web.Result;
import com.ksptool.bio.biz.qf.model.qftodo.dto.ApproveQfTodoDto;
import com.ksptool.bio.biz.qf.model.qftodo.dto.GetQfTodoListDto;
import com.ksptool.bio.biz.qf.model.qftodo.vo.GetQfTodoDetailsVo;
import com.ksptool.bio.biz.qf.model.qftodo.vo.GetQfTodoListVo;
import com.ksptool.bio.biz.qf.service.QfTodoService;
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
 * QF-待办事项
 * 
 * @author WangQingHua(603484930@qq.com)
 * @author (Ish)Yuumi(1144150092@qq.com)
 * @author Akkarin(1075613357@qq.com)
 * @author KspTool(ksptool@outlook.com)
 * @since 2026-04-16
 * @license Proprietary
 * 版权所有 (c) 2026 KspTool及其贡献者保留所有权利。
 * 未经事先书面许可，严禁任何形式的复制或分发。
 */
@PrintLog
@RestController
@RequestMapping("/qfTodo")
@Tag(name = "QF-待办事项", description = "QF-待办事项")
@Slf4j
public class QfTodoController {

    @Autowired
    private QfTodoService qfTodoService;

    @PreAuthorize("@auth.hasCode('qf:todo:view')")
    @PostMapping("/getQfTodoList")
    @Operation(summary = "查询待办事项列表")
    public PageResult<GetQfTodoListVo> getQfTodoList(@RequestBody @Valid GetQfTodoListDto dto) throws Exception {
        return qfTodoService.getQfTodoList(dto);
    }

    @PreAuthorize("@auth.hasCode('qf:todo:view')")
    @Operation(summary = "查询待办事项详情")
    @PostMapping("/getQfTodoDetails")
    public Result<GetQfTodoDetailsVo> getQfTodoDetails(@RequestBody @Valid CommonIdDto dto) throws Exception {
        GetQfTodoDetailsVo details = qfTodoService.getQfTodoDetails(dto);
        if (details == null) {
            return Result.error("无数据");
        }
        return Result.success(details);
    }

    @PreAuthorize("@auth.hasCode('qf:todo:remove')")
    @Operation(summary = "删除待办事项")
    @PostMapping("/removeQfTodo")
    public Result<String> removeQfTodo(@RequestBody @Valid CommonIdDto dto) throws Exception {
        qfTodoService.removeQfTodo(dto);
        return Result.success("操作成功");
    }

    @PreAuthorize("@auth.hasCode('qf:todo:approve')")
    @Operation(summary = "审批待办事项")
    @PostMapping("/approveQfTodo")
    public Result<String> approveQfTodo(@RequestBody @Valid ApproveQfTodoDto dto) throws Exception {
        qfTodoService.approveQfTodo(dto);
        return Result.success("操作成功");
    }

}
