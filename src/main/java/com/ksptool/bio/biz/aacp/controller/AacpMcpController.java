package com.ksptool.bio.biz.aacp.controller;

import com.ksptool.assembly.entity.web.CommonIdDto;
import com.ksptool.assembly.entity.web.PageResult;
import com.ksptool.assembly.entity.web.Result;
import com.ksptool.bio.biz.aacp.model.mcp.dto.AddAacpMcpDto;
import com.ksptool.bio.biz.aacp.model.mcp.dto.EditAacpMcpDto;
import com.ksptool.bio.biz.aacp.model.mcp.dto.GetAacpMcpListDto;
import com.ksptool.bio.biz.aacp.model.mcp.vo.GetAacpMcpDetailsVo;
import com.ksptool.bio.biz.aacp.model.mcp.vo.GetAacpMcpListVo;
import com.ksptool.bio.biz.aacp.service.AacpMcpService;
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
@RequestMapping("/aacpMcp")
@Tag(name = "MCP服务器", description = "MCP服务器")
@Slf4j
public class AacpMcpController {

    @Autowired
    private AacpMcpService aacpMcpService;

    @PreAuthorize("@auth.hasCode('aacp:mcp:view')")
    @PostMapping("/getAacpMcpList")
    @Operation(summary = "查询MCP服务器列表")
    public PageResult<GetAacpMcpListVo> getAacpMcpList(@RequestBody @Valid GetAacpMcpListDto dto) throws Exception {
        return aacpMcpService.getAacpMcpList(dto);
    }

    @PreAuthorize("@auth.hasCode('aacp:mcp:add')")
    @Operation(summary = "新增MCP服务器")
    @PostMapping("/addAacpMcp")
    public Result<String> addAacpMcp(@RequestBody @Valid AddAacpMcpDto dto) throws Exception {
        aacpMcpService.addAacpMcp(dto);
        return Result.success("新增成功");
    }

    @PreAuthorize("@auth.hasCode('aacp:mcp:edit')")
    @Operation(summary = "编辑MCP服务器")
    @PostMapping("/editAacpMcp")
    public Result<String> editAacpMcp(@RequestBody @Valid EditAacpMcpDto dto) throws Exception {
        aacpMcpService.editAacpMcp(dto);
        return Result.success("修改成功");
    }

    @PreAuthorize("@auth.hasCode('aacp:mcp:view')")
    @Operation(summary = "查询MCP服务器详情")
    @PostMapping("/getAacpMcpDetails")
    public Result<GetAacpMcpDetailsVo> getAacpMcpDetails(@RequestBody @Valid CommonIdDto dto) throws Exception {
        GetAacpMcpDetailsVo details = aacpMcpService.getAacpMcpDetails(dto);
        if (details == null) {
            return Result.error("无数据");
        }
        return Result.success(details);
    }

    @PreAuthorize("@auth.hasCode('aacp:mcp:remove')")
    @Operation(summary = "删除MCP服务器")
    @PostMapping("/removeAacpMcp")
    public Result<String> removeAacpMcp(@RequestBody @Valid CommonIdDto dto) throws Exception {
        aacpMcpService.removeAacpMcp(dto);
        return Result.success("操作成功");
    }
}
