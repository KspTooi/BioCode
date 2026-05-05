package com.ksptool.bio.biz.auth.controller;

import com.ksptool.assembly.entity.web.CommonIdDto;
import com.ksptool.assembly.entity.web.PageResult;
import com.ksptool.assembly.entity.web.Result;
import com.ksptool.bio.biz.auth.common.aop.RowScope;
import com.ksptool.bio.biz.auth.model.session.dto.GetSessionListDto;
import com.ksptool.bio.biz.auth.model.session.vo.GetSessionDetailsVo;
import com.ksptool.bio.biz.auth.model.session.vo.GetSessionListVo;
import com.ksptool.bio.biz.auth.service.SessionService;
import com.ksptool.bio.commons.annotation.PrintLog;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@PrintLog
@RestController
@RequestMapping("/session")
@Tag(name = "AUTH-用户会话管理", description = "用户会话管理")
public class SessionController {

    @Autowired
    private SessionService service;

    @RowScope
    @PreAuthorize("@auth.hasCode('auth:session:list')")
    @Operation(summary = "获取在线用户列表")
    @PostMapping("getSessionList")
    public PageResult<GetSessionListVo> getSessionList(@RequestBody @Valid GetSessionListDto dto) {
        return service.getSessionList(dto);
    }

    @RowScope
    @PreAuthorize("@auth.hasCode('auth:session:details')")
    @Operation(summary = "获取在线用户详情")
    @PostMapping("getSessionDetails")
    public Result<GetSessionDetailsVo> getSessionDetails(@RequestBody @Valid CommonIdDto dto) throws Exception {
        return Result.success(service.getSessionDetails(dto.getId()));
    }

    @RowScope
    @PreAuthorize("@auth.hasCode('auth:session:remove')")
    @Operation(summary = "关闭在线用户会话", description = "强退用户时使用")
    @PostMapping("closeSession")
    public Result<String> closeSession(@RequestBody @Valid CommonIdDto dto) throws Exception {
        service.closeSessionByPrimaryKey(dto.getId());
        return Result.success("关闭成功");
    }

    
}
