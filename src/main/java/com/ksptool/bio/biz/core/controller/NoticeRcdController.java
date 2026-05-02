package com.ksptool.bio.biz.core.controller;

import com.ksptool.bio.biz.core.model.noticercd.dto.GetUserNoticeRcdListDto;
import com.ksptool.bio.biz.core.model.noticercd.vo.GetNoticeRcdDetailsVo;
import com.ksptool.bio.biz.core.model.noticercd.vo.GetUserNoticeRcdListVo;
import com.ksptool.bio.biz.core.service.NoticeRcdService;
import com.ksptool.assembly.entity.web.CommonIdDto;
import com.ksptool.assembly.entity.web.PageResult;
import com.ksptool.assembly.entity.web.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/noticeRcd")
@Tag(name = "CORE-消息通知管理(用户记录)", description = "用户通知记录(接收状态)")
@Slf4j
public class NoticeRcdController {

    @Autowired
    private NoticeRcdService noticeRcdService;

    @PostMapping("/getNoticeRcdList")
    @Operation(summary = "查询当前用户通知记录列表")
    public PageResult<GetUserNoticeRcdListVo> getUserNoticeRcdList(@RequestBody @Valid GetUserNoticeRcdListDto dto) throws Exception {
        return noticeRcdService.getUserNoticeRcdList(dto);
    }

    @PostMapping("/getUserNoticeCount")
    @Operation(summary = "获取当前用户未读通知数量")
    public Result<Integer> getUserNoticeCount() throws Exception {
        return Result.success(noticeRcdService.getUserNoticeCount());
    }

    @PostMapping("/readAllNoticeRcd")
    @Operation(summary = "阅读全部用户通知记录")
    public Result<String> readAllUserNoticeRcd() throws Exception {
        noticeRcdService.readAllUserNoticeRcd();
        return Result.success("操作成功");
    }

    @Operation(summary = "查询用户通知记录详情")
    @PostMapping("/getUserNoticeRcdDetails")
    public Result<GetNoticeRcdDetailsVo> getUserNoticeRcdDetails(@RequestBody @Valid CommonIdDto dto) throws Exception {
        GetNoticeRcdDetailsVo details = noticeRcdService.getUserNoticeRcdDetails(dto);
        if (details == null) {
            return Result.error("无数据");
        }
        return Result.success(details);
    }

    @Operation(summary = "删除用户通知记录")
    @PostMapping("/removeNoticeRcd")
    public Result<String> removeNoticeRcd(@RequestBody @Valid CommonIdDto dto) throws Exception {
        noticeRcdService.removeNoticeRcd(dto);
        return Result.success("操作成功");
    }

}
