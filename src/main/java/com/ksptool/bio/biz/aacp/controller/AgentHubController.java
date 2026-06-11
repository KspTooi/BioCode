package com.ksptool.bio.biz.aacp.controller;

import com.ksptool.assembly.entity.web.CommonIdDto;
import com.ksptool.assembly.entity.web.PageResult;
import com.ksptool.assembly.entity.web.Result;
import com.ksptool.bio.biz.aacp.model.agenthub.dto.AddAgentHubDto;
import com.ksptool.bio.biz.aacp.model.agenthub.dto.EditAgentHubDto;
import com.ksptool.bio.biz.aacp.model.agenthub.dto.GetAgentHubListDto;
import com.ksptool.bio.biz.aacp.model.agenthub.vo.GetAgentHubDetailsVo;
import com.ksptool.bio.biz.aacp.model.agenthub.vo.GetAgentHubListVo;
import com.ksptool.bio.biz.aacp.service.AgentHubService;
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
@RequestMapping("/agentHub")
@Tag(name = "智能体枢纽", description = "智能体枢纽")
@Slf4j
public class AgentHubController {

    @Autowired
    private AgentHubService agentHubService;

    @PreAuthorize("@auth.hasCode('aacp:agenthub:view')")
    @PostMapping("/getAgentHubList")
    @Operation(summary = "查询智能体枢纽列表")
    public PageResult<GetAgentHubListVo> getAgentHubList(@RequestBody @Valid GetAgentHubListDto dto) throws Exception {
        return agentHubService.getAgentHubList(dto);
    }

    @PreAuthorize("@auth.hasCode('aacp:agenthub:add')")
    @Operation(summary = "新增智能体枢纽")
    @PostMapping("/addAgentHub")
    public Result<String> addAgentHub(@RequestBody @Valid AddAgentHubDto dto) throws Exception {
        agentHubService.addAgentHub(dto);
        return Result.success("新增成功");
    }

    @PreAuthorize("@auth.hasCode('aacp:agenthub:edit')")
    @Operation(summary = "编辑智能体枢纽")
    @PostMapping("/editAgentHub")
    public Result<String> editAgentHub(@RequestBody @Valid EditAgentHubDto dto) throws Exception {
        agentHubService.editAgentHub(dto);
        return Result.success("修改成功");
    }

    @PreAuthorize("@auth.hasCode('aacp:agenthub:view')")
    @Operation(summary = "查询智能体枢纽详情")
    @PostMapping("/getAgentHubDetails")
    public Result<GetAgentHubDetailsVo> getAgentHubDetails(@RequestBody @Valid CommonIdDto dto) throws Exception {
        GetAgentHubDetailsVo details = agentHubService.getAgentHubDetails(dto);
        if (details == null) {
            return Result.error("无数据");
        }
        return Result.success(details);
    }

    @PreAuthorize("@auth.hasCode('aacp:agenthub:remove')")
    @Operation(summary = "删除智能体枢纽")
    @PostMapping("/removeAgentHub")
    public Result<String> removeAgentHub(@RequestBody @Valid CommonIdDto dto) throws Exception {
        agentHubService.removeAgentHub(dto);
        return Result.success("操作成功");
    }
}
