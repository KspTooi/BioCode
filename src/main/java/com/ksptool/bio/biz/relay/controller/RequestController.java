package com.ksptool.bio.biz.relay.controller;

import com.ksptool.assembly.entity.web.CommonIdDto;
import com.ksptool.assembly.entity.web.PageResult;
import com.ksptool.assembly.entity.web.Result;
import com.ksptool.bio.biz.relay.model.request.GetRequestDetailsVo;
import com.ksptool.bio.biz.relay.model.request.GetRequestListDto;
import com.ksptool.bio.biz.relay.model.request.GetRequestListVo;
import com.ksptool.bio.biz.relay.service.RequestService;
import com.ksptool.bio.commons.annotation.PrintLog;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Slf4j
@PrintLog
@RestController
@RequestMapping("/request")
@Tag(name = "RELAY-中继器请求", description = "中继器请求管理")
public class RequestController {

    @Autowired
    private RequestService requestService;

    @PostMapping("/getRequestList")
    @Operation(summary = "获取中继器请求列表")
    public PageResult<GetRequestListVo> getRequestList(@RequestBody @Valid GetRequestListDto dto) {

        //开始时间与结束时间不可单独填写
        if ((dto.getStartTime() == null && dto.getEndTime() != null) || (dto.getStartTime() != null && dto.getEndTime() == null)) {
            return PageResult.error("开始时间与结束时间必须同时填写");
        }

        if (dto.getStartTime() != null && dto.getEndTime() != null) {
            //开始、结束时间区间范围不可以超过15天
            long days = ChronoUnit.DAYS.between(dto.getStartTime(), dto.getEndTime());
            if (days > 15) {
                return PageResult.error("时间范围不可以超过15天");
            }
        }

        //如果不填写开始时间结束时间 则以今天的23:59:59开始 向前推15天
        if (dto.getStartTime() == null || dto.getEndTime() == null) {
            LocalDateTime startOfToday = LocalDateTime.now().withHour(23).withMinute(59).withSecond(59).withNano(999999999);
            LocalDateTime fifteenDaysAgo = startOfToday.minusDays(15);
            dto.setStartTime(fifteenDaysAgo);
            dto.setEndTime(startOfToday);
        }

        return requestService.getRequestList(dto);
    }

    @PostMapping("/getRequestDetails")
    @Operation(summary = "获取中继器请求详情")
    public Result<GetRequestDetailsVo> getRequestDetails(@RequestBody @Valid CommonIdDto dto) {
        return Result.success(requestService.getRequestDetails(dto.getId()));
    }


}
