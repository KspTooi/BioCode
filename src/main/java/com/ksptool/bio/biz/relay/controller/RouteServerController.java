package com.ksptool.bio.biz.relay.controller;

import com.ksptool.assembly.entity.web.CommonIdDto;
import com.ksptool.assembly.entity.web.PageResult;
import com.ksptool.assembly.entity.web.Result;
import com.ksptool.bio.biz.relay.model.routeserver.dto.AddRouteServerDto;
import com.ksptool.bio.biz.relay.model.routeserver.dto.EditRouteServerDto;
import com.ksptool.bio.biz.relay.model.routeserver.dto.GetRouteServerListDto;
import com.ksptool.bio.biz.relay.model.routeserver.vo.GetRouteServerDetailsVo;
import com.ksptool.bio.biz.relay.model.routeserver.vo.GetRouteServerListVo;
import com.ksptool.bio.biz.relay.service.RouteServerService;
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
@RequestMapping("/routeServer")
@Tag(name = "RELAY-路由服务器", description = "路由服务器管理")
@Slf4j
public class RouteServerController {

    @Autowired
    private RouteServerService routeServerService;

    @PreAuthorize("@auth.hasCode('relay:server:view')")
    @PostMapping("/getRouteServerList")
    @Operation(summary = "查询路由服务器列表")
    public PageResult<GetRouteServerListVo> getRouteServerList(@RequestBody @Valid GetRouteServerListDto dto) throws Exception {
        return routeServerService.getRouteServerList(dto);
    }

    @PreAuthorize("@auth.hasCode('relay:server:add')")
    @Operation(summary = "新增路由服务器")
    @PostMapping("/addRouteServer")
    public Result<String> addRouteServer(@RequestBody @Valid AddRouteServerDto dto) throws Exception {
        routeServerService.addRouteServer(dto);
        return Result.success("新增成功");
    }

    @PreAuthorize("@auth.hasCode('relay:server:edit')")
    @Operation(summary = "编辑路由服务器")
    @PostMapping("/editRouteServer")
    public Result<String> editRouteServer(@RequestBody @Valid EditRouteServerDto dto) throws Exception {
        routeServerService.editRouteServer(dto);
        return Result.success("修改成功");
    }

    @PreAuthorize("@auth.hasCode('relay:server:view')")
    @Operation(summary = "查询路由服务器详情")
    @PostMapping("/getRouteServerDetails")
    public Result<GetRouteServerDetailsVo> getRouteServerDetails(@RequestBody @Valid CommonIdDto dto) throws Exception {
        GetRouteServerDetailsVo details = routeServerService.getRouteServerDetails(dto);
        if (details == null) {
            return Result.error("无数据");
        }
        return Result.success(details);
    }

    @PreAuthorize("@auth.hasCode('relay:server:remove')")
    @Operation(summary = "删除路由服务器")
    @PostMapping("/removeRouteServer")
    public Result<String> removeRouteServer(@RequestBody @Valid CommonIdDto dto) throws Exception {
        routeServerService.removeRouteServer(dto);
        return Result.success("操作成功");
    }

}
