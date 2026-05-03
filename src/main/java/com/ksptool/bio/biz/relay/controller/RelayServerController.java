package com.ksptool.bio.biz.relay.controller;

import com.ksptool.assembly.entity.exception.BizException;
import com.ksptool.assembly.entity.web.CommonIdDto;
import com.ksptool.assembly.entity.web.PageResult;
import com.ksptool.assembly.entity.web.Result;
import com.ksptool.bio.biz.relay.model.relayserver.*;
import com.ksptool.bio.biz.relay.service.RelayServerService;
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

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/relayServer")
@Tag(name = "RELAY-中继服务器", description = "中继服务器管理")
public class RelayServerController {


    @Autowired
    private RelayServerService relayServerService;


    @PreAuthorize("@auth.hasCode('relay:server:view')")
    @Operation(summary = "获取中继服务器列表")
    @PostMapping("/getRelayServerList")
    public PageResult<GetRelayServerListVo> getRelayServerList(@RequestBody @Valid GetRelayServerListDto dto) {
        return relayServerService.getRelayServerList(dto);
    }


    @PreAuthorize("@auth.hasCode('relay:server:add')")
    @Operation(summary = "添加中继服务器")
    @PostMapping("/addRelayServer")
    public Result<String> addRelayServer(@RequestBody @Valid AddRelayServerDto dto) throws BizException {

        String validate = dto.validate();

        if (validate != null) {
            throw new BizException(validate);
        }

        relayServerService.addRelayServer(dto);
        return Result.success("添加中继服务器成功");
    }


    @PreAuthorize("@auth.hasCode('relay:server:edit')")
    @Operation(summary = "编辑中继服务器")
    @PostMapping("/editRelayServer")
    public Result<String> editRelayServer(@RequestBody @Valid EditRelayServerDto dto) throws BizException {

        String validate = dto.validate();
        if (validate != null) {
            throw new BizException(validate);
        }

        relayServerService.editRelayServer(dto);
        return Result.success("编辑中继服务器成功");
    }


    @PreAuthorize("@auth.hasCode('relay:server:view')")
    @Operation(summary = "获取中继服务器详情")
    @PostMapping("/getRelayServerDetails")
    public Result<GetRelayServerDetailsVo> getRelayServerDetails(@RequestBody @Valid CommonIdDto dto) throws BizException {
        return Result.success(relayServerService.getRelayServerDetails(dto.getId()));
    }

    @PreAuthorize("@auth.hasCode('relay:server:remove')")
    @Operation(summary = "删除中继服务器")
    @PostMapping("/removeRelayServer")
    public Result<String> removeRelayServer(@RequestBody @Valid CommonIdDto dto) throws BizException {
        relayServerService.removeRelayServer(dto.getId());
        return Result.success("删除中继服务器成功");
    }

    @PreAuthorize("@auth.hasCode('relay:server:edit')")
    @Operation(summary = "启动中继服务器")
    @PostMapping("/startRelayServer")
    public Result<String> startRelayServer(@RequestBody @Valid CommonIdDto dto) throws BizException {
        return relayServerService.startRelayServer(dto.getId());
    }

    @PreAuthorize("@auth.hasCode('relay:server:edit')")
    @Operation(summary = "停止中继服务器")
    @PostMapping("/stopRelayServer")
    public Result<String> stopRelayServer(@RequestBody @Valid CommonIdDto dto) throws BizException {
        return relayServerService.stopRelayServer(dto.getId());
    }

    @PreAuthorize("@auth.hasCode('relay:server:edit')")
    @Operation(summary = "获取中继服务器路由状态")
    @PostMapping("/getRelayServerRouteState")
    public Result<List<GetRelayServerRouteStateVo>> getRelayServerRouteState(@RequestBody @Valid CommonIdDto dto) throws BizException {
        return Result.success(relayServerService.getRelayServerRouteState(dto.getId()));
    }

    @PreAuthorize("@auth.hasCode('relay:server:edit')")
    @Operation(summary = "重置熔断状态")
    @PostMapping("/resetRelayServerBreaker")
    public Result<String> resetRelayServerBreaker(@RequestBody @Valid ResetRelayServerBreakerDto dto) throws BizException {
        relayServerService.resetRelayServerBreaker(dto);
        return Result.success("重置中继服务器熔断器成功");
    }

}
