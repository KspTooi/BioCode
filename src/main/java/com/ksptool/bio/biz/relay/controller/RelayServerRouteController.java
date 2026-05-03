package com.ksptool.bio.biz.relay.controller;

import com.ksptool.assembly.entity.web.CommonIdDto;
import com.ksptool.assembly.entity.web.PageResult;
import com.ksptool.assembly.entity.web.Result;
import com.ksptool.bio.biz.relay.model.relayserverroute.dto.AddRelayServerRouteDto;
import com.ksptool.bio.biz.relay.model.relayserverroute.dto.EditRelayServerRouteDto;
import com.ksptool.bio.biz.relay.model.relayserverroute.dto.GetRelayServerRouteListDto;
import com.ksptool.bio.biz.relay.model.relayserverroute.vo.GetRelayServerRouteDetailsVo;
import com.ksptool.bio.biz.relay.model.relayserverroute.vo.GetRelayServerRouteListVo;
import com.ksptool.bio.biz.relay.service.RelayServerRouteService;
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
@RequestMapping("/relayServerRoute")
@Tag(name = "RELAY-中继路由策略", description = "中继路由策略管理")
@Slf4j
public class RelayServerRouteController {

    @Autowired
    private RelayServerRouteService relayServerRouteService;


    @PreAuthorize("@auth.hasCode('relay:route:view')")
    @PostMapping("/getRelayServerRouteList")
    @Operation(summary = "查询中继路由策略列表")
    public PageResult<GetRelayServerRouteListVo> getRelayServerRouteList(@RequestBody @Valid GetRelayServerRouteListDto dto) throws Exception {
        return relayServerRouteService.getRelayServerRouteList(dto);
    }

    @PreAuthorize("@auth.hasCode('relay:route:add')")
    @Operation(summary = "新增中继路由策略")
    @PostMapping("/addRelayServerRoute")
    public Result<String> addRelayServerRoute(@RequestBody @Valid AddRelayServerRouteDto dto) throws Exception {
        relayServerRouteService.addRelayServerRoute(dto);
        return Result.success("新增成功");
    }

    @PreAuthorize("@auth.hasCode('relay:route:edit')")
    @Operation(summary = "编辑中继路由策略")
    @PostMapping("/editRelayServerRoute")
    public Result<String> editRelayServerRoute(@RequestBody @Valid EditRelayServerRouteDto dto) throws Exception {
        relayServerRouteService.editRelayServerRoute(dto);
        return Result.success("修改成功");
    }

    @PreAuthorize("@auth.hasCode('relay:route:view')")
    @Operation(summary = "查询中继路由策略详情")
    @PostMapping("/getRelayServerRouteDetails")
    public Result<GetRelayServerRouteDetailsVo> getRelayServerRouteDetails(@RequestBody @Valid CommonIdDto dto) throws Exception {
        GetRelayServerRouteDetailsVo details = relayServerRouteService.getRelayServerRouteDetails(dto);
        if (details == null) {
            return Result.error("无数据");
        }
        return Result.success(details);
    }

    @PreAuthorize("@auth.hasCode('relay:route:remove')")
    @Operation(summary = "删除中继路由策略")
    @PostMapping("/removeRelayServerRoute")
    public Result<String> removeRelayServerRoute(@RequestBody @Valid CommonIdDto dto) throws Exception {
        relayServerRouteService.removeRelayServerRoute(dto);
        return Result.success("操作成功");
    }

}
