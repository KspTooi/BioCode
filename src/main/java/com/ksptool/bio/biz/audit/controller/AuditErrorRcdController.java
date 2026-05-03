package com.ksptool.bio.biz.audit.controller;


import com.ksptool.assembly.entity.web.CommonIdDto;
import com.ksptool.assembly.entity.web.PageResult;
import com.ksptool.assembly.entity.web.Result;
import com.ksptool.bio.biz.audit.model.auditerrorrcd.dto.GetAuditErrorRcdListDto;
import com.ksptool.bio.biz.audit.model.auditerrorrcd.vo.GetAuditErrorRcdDetailsVo;
import com.ksptool.bio.biz.audit.model.auditerrorrcd.vo.GetAuditErrorRcdListVo;
import com.ksptool.bio.biz.audit.service.AuditErrorRcdService;
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
@RequestMapping("/auditErrorRcd")
@Tag(name = "AUDIT-系统错误记录", description = "系统错误记录")
@Slf4j
public class AuditErrorRcdController {

    @Autowired
    private AuditErrorRcdService auditErrorRcdService;

    @PreAuthorize("@auth.hasCode('audit:error:view')")
    @PostMapping("/getAuditErrorRcdList")
    @Operation(summary = "查询系统错误记录列表")
    public PageResult<GetAuditErrorRcdListVo> getAuditErrorRcdList(@RequestBody @Valid GetAuditErrorRcdListDto dto) throws Exception {
        return auditErrorRcdService.getAuditErrorRcdList(dto);
    }

    @PreAuthorize("@auth.hasCode('audit:error:view')")
    @Operation(summary = "查询系统错误记录详情")
    @PostMapping("/getAuditErrorRcdDetails")
    public Result<GetAuditErrorRcdDetailsVo> getAuditErrorRcdDetails(@RequestBody @Valid CommonIdDto dto) throws Exception {
        GetAuditErrorRcdDetailsVo details = auditErrorRcdService.getAuditErrorRcdDetails(dto);
        if (details == null) {
            return Result.error("无数据");
        }
        return Result.success(details);
    }

    @PreAuthorize("@auth.hasCode('audit:error:remove')")
    @Operation(summary = "删除系统错误记录")
    @PostMapping("/removeAuditErrorRcd")
    public Result<String> removeAuditErrorRcd(@RequestBody @Valid CommonIdDto dto) throws Exception {
        auditErrorRcdService.removeAuditErrorRcd(dto);
        return Result.success("操作成功");
    }

}
