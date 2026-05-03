package com.ksptool.bio.biz.relay.controller;

import com.ksptool.assembly.entity.web.CommonIdDto;
import com.ksptool.assembly.entity.web.PageResult;
import com.ksptool.assembly.entity.web.Result;
import com.ksptool.bio.biz.relay.model.routerule.dto.AddRouteRuleDto;
import com.ksptool.bio.biz.relay.model.routerule.dto.EditRouteRuleDto;
import com.ksptool.bio.biz.relay.model.routerule.dto.GetRouteRuleListDto;
import com.ksptool.bio.biz.relay.model.routerule.vo.GetRouteRuleDetailsVo;
import com.ksptool.bio.biz.relay.model.routerule.vo.GetRouteRuleListVo;
import com.ksptool.bio.biz.relay.service.RouteRuleService;
import com.ksptool.bio.commons.annotation.PrintLog;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@PrintLog
@RestController
@RequestMapping("/routeRule")
@Tag(name = "RELAY-路由规则", description = "路由规则管理")
@Slf4j
public class RouteRuleController {

    @Autowired
    private RouteRuleService routeRuleService;

    @PreAuthorize("@auth.hasCode('relay:rule:view')")
    @Operation(summary = "查询路由规则列表")
    @PostMapping("/getRouteRuleList")
    public PageResult<GetRouteRuleListVo> getRouteRuleList(@RequestBody @Valid GetRouteRuleListDto dto) throws Exception {
        return routeRuleService.getRouteRuleList(dto);
    }

    @PreAuthorize("@auth.hasCode('relay:rule:add')")
    @Operation(summary = "新增路由规则")
    @PostMapping("/addRouteRule")
    public Result<String> addRouteRule(@RequestBody @Valid AddRouteRuleDto dto) throws Exception {

        //验证参数
        String validate = dto.validate();

        if (StringUtils.isNotBlank(validate)) {
            return Result.error(validate);
        }

        routeRuleService.addRouteRule(dto);
        return Result.success("新增成功");
    }

    @PreAuthorize("@auth.hasCode('relay:rule:edit')")
    @Operation(summary = "编辑路由规则")
    @PostMapping("/editRouteRule")
    public Result<String> editRouteRule(@RequestBody @Valid EditRouteRuleDto dto) throws Exception {

        //验证参数
        String validate = dto.validate();

        if (StringUtils.isNotBlank(validate)) {
            return Result.error(validate);
        }

        routeRuleService.editRouteRule(dto);
        return Result.success("编辑成功");
    }

    @PreAuthorize("@auth.hasCode('relay:rule:view')")
    @Operation(summary = "查询路由规则详情")
    @PostMapping("/getRouteRuleDetails")
    public Result<GetRouteRuleDetailsVo> getRouteRuleDetails(@RequestBody @Valid CommonIdDto dto) throws Exception {
        GetRouteRuleDetailsVo details = routeRuleService.getRouteRuleDetails(dto);
        if (details == null) {
            return Result.error("无数据");
        }
        return Result.success(details);
    }

    @PreAuthorize("@auth.hasCode('relay:rule:remove')")
    @Operation(summary = "删除路由规则")
    @PostMapping("/removeRouteRule")
    public Result<String> removeRouteRule(@RequestBody @Valid CommonIdDto dto) throws Exception {
        routeRuleService.removeRouteRule(dto);
        return Result.success("操作成功");
    }

}
