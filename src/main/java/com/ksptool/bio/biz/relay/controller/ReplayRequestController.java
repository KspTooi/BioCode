package com.ksptool.bio.biz.relay.controller;

import com.ksptool.assembly.entity.exception.AuthException;
import com.ksptool.assembly.entity.exception.BizException;
import com.ksptool.assembly.entity.web.CommonIdDto;
import com.ksptool.assembly.entity.web.PageResult;
import com.ksptool.assembly.entity.web.Result;
import com.ksptool.bio.biz.relay.model.replayrequest.*;
import com.ksptool.bio.biz.relay.service.ReplayRequestService;
import com.ksptool.bio.commons.annotation.PrintLog;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@PrintLog
@Controller
@RestController
@RequestMapping("/replayRequest")
@Tag(name = "RELAY-简单请求重放", description = "简单请求重放管理")
public class ReplayRequestController {

    @Autowired
    private ReplayRequestService replayRequestService;

    /**
     * 重放某原始请求
     *
     * @param dto 请求参数
     * @return 重放请求结果
     * @throws BizException 业务异常
     */
    @PostMapping("/replayRequest")
    public Result<String> replayRequest(@RequestBody @Valid Map<String, String> dto) throws Exception {

        String requestId = dto.get("requestId");

        if (StringUtils.isBlank(requestId)) {
            return Result.error("请求ID必传");
        }

        replayRequestService.replayRequest(dto.get("requestId"));
        return Result.success("success");
    }

    @PostMapping("/getOriginRequest")
    public Result<GetOriginRequestVo> getOriginRequest(@RequestBody @Valid GetOriginRequestDto dto) throws Exception {
        return Result.success(replayRequestService.getOriginRequest(dto.getRequestId()));
    }

    @PostMapping("/getReplayRequestList")
    public PageResult<GetReplayRequestListVo> getReplayRequestList(@RequestBody @Valid GetReplayRequestListDto dto) throws AuthException {
        return replayRequestService.getReplayRequestList(dto);
    }

    @PostMapping("/getReplayRequestDetails")
    public Result<GetReplayRequestDetailsVo> getReplayRequestDetails(@RequestBody @Valid CommonIdDto dto) {
        return Result.success(replayRequestService.getReplayRequestDetails(dto.getId()));
    }


}
