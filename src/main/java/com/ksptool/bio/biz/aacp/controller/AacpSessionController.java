package com.ksptool.bio.biz.aacp.controller;

import com.ksptool.assembly.entity.web.PageResult;
import com.ksptool.assembly.entity.web.Result;
import com.ksptool.bio.biz.aacp.model.session.dto.CloseSessionDto;
import com.ksptool.bio.biz.aacp.model.session.vo.GetOnlineSessionListVo;
import com.ksptool.bio.biz.aacp.service.AacpAccessService;
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

import java.util.List;
import java.util.stream.Collectors;

import static com.ksptool.entities.Entities.as;

@PrintLog
@RestController
@RequestMapping("/aacpSession")
@Tag(name = "AACP在线会话", description = "AACP在线会话管理")
public class AacpSessionController {

    @Autowired
    private AacpAccessService aacpAccessService;

    @PreAuthorize("@auth.hasCode('aacp:session:view')")
    @PostMapping("/getOnlineSessionList")
    @Operation(summary = "查询在线会话列表")
    public PageResult<GetOnlineSessionListVo> getOnlineSessionList() {
        var sessions = aacpAccessService.getOnlineSessionList();
        if (sessions.isEmpty()) {
            return PageResult.successWithEmpty();
        }
        List<GetOnlineSessionListVo> vos = sessions.stream()
                .map(s -> as(s, GetOnlineSessionListVo.class))
                .collect(Collectors.toList());
        return PageResult.success(vos, vos.size());
    }

    @PreAuthorize("@auth.hasCode('aacp:session:remove')")
    @PostMapping("/closeSession")
    @Operation(summary = "关闭在线会话")
    public Result<String> closeSession(@RequestBody @Valid CloseSessionDto dto) {
        aacpAccessService.closeSession(dto.getSessionId());
        return Result.success("关闭成功");
    }
}
