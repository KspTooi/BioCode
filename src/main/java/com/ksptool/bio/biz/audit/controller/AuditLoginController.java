package com.ksptool.bio.biz.audit.controller;

import com.ksptool.assembly.entity.web.CommonIdDto;
import com.ksptool.assembly.entity.web.PageResult;
import com.ksptool.assembly.entity.web.Result;
import com.ksptool.bio.biz.audit.model.auditlogin.dto.GetAuditLoginListDto;
import com.ksptool.bio.biz.audit.model.auditlogin.vo.GetAuditLoginDetailsVo;
import com.ksptool.bio.biz.audit.model.auditlogin.vo.GetAuditLoginListVo;
import com.ksptool.bio.biz.audit.service.AuditLoginService;
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


@PrintLog
@RestController
@RequestMapping("/auditLogin")
@Tag(name = "AUDIT-登录审计日志", description = "登录审计日志")
@Slf4j
public class AuditLoginController {

    @Autowired
    private AuditLoginService auditLoginService;

    @PreAuthorize("@auth.hasCode('audit:login:view')")
    @PostMapping("/getAuditLoginList")
    @Operation(summary = "获取登录日志列表")
    public PageResult<GetAuditLoginListVo> getAuditLoginList(@RequestBody @Valid GetAuditLoginListDto dto) throws Exception {
        return auditLoginService.getAuditLoginList(dto);
    }

    @PreAuthorize("@auth.hasCode('audit:login:view')")
    @Operation(summary = "获取登录日志详情")
    @PostMapping("/getAuditLoginDetails")
    public Result<GetAuditLoginDetailsVo> getAuditLoginDetails(@RequestBody @Valid CommonIdDto dto) throws Exception {
        GetAuditLoginDetailsVo details = auditLoginService.getAuditLoginDetails(dto);
        if (details == null) {
            return Result.error("无数据");
        }
        return Result.success(details);
    }

    @PreAuthorize("@auth.hasCode('audit:login:remove')")
    @Operation(summary = "删除登录日志")
    @PostMapping("/removeAuditLogin")
    public Result<String> removeAuditLogin(@RequestBody @Valid CommonIdDto dto) throws Exception {
        auditLoginService.removeAuditLogin(dto);
        return Result.success("操作成功");
    }

}
