package com.ksptool.bio.biz.aacp.controller;

import com.ksptool.assembly.entity.web.CommonIdDto;
import com.ksptool.assembly.entity.web.PageResult;
import com.ksptool.assembly.entity.web.Result;
import com.ksptool.bio.biz.aacp.model.cap.dto.AddCapDto;
import com.ksptool.bio.biz.aacp.model.cap.dto.EditCapDto;
import com.ksptool.bio.biz.aacp.model.cap.dto.GetCapListDto;
import com.ksptool.bio.biz.aacp.model.cap.vo.GetCapDetailsVo;
import com.ksptool.bio.biz.aacp.model.cap.vo.GetCapListVo;
import com.ksptool.bio.biz.aacp.service.CapService;
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
@RequestMapping("/cap")
@Tag(name = "能力包", description = "能力包")
@Slf4j
public class CapController {

    @Autowired
    private CapService capService;

    @PreAuthorize("@auth.hasCode('aacp:cap:view')")
    @PostMapping("/getCapList")
    @Operation(summary = "查询能力包列表")
    public PageResult<GetCapListVo> getCapList(@RequestBody @Valid GetCapListDto dto) throws Exception {
        return capService.getCapList(dto);
    }

    @PreAuthorize("@auth.hasCode('aacp:cap:add')")
    @PostMapping("/addCap")
    @Operation(summary = "新增能力包")
    public Result<String> addCap(@RequestBody @Valid AddCapDto dto) throws Exception {
        capService.addCap(dto);
        return Result.success("新增成功");
    }

    @PreAuthorize("@auth.hasCode('aacp:cap:edit')")
    @PostMapping("/editCap")
    @Operation(summary = "编辑能力包")
    public Result<String> editCap(@RequestBody @Valid EditCapDto dto) throws Exception {
        capService.editCap(dto);
        return Result.success("修改成功");
    }

    @PreAuthorize("@auth.hasCode('aacp:capability:view')")
    @PostMapping("/getCapDetails")
    @Operation(summary = "查询能力包详情")
    public Result<GetCapDetailsVo> getCapDetails(@RequestBody @Valid CommonIdDto dto) throws Exception {
        GetCapDetailsVo details = capService.getCapDetails(dto);
        if (details == null) {
            return Result.error("无数据");
        }
        return Result.success(details);
    }

    @PreAuthorize("@auth.hasCode('aacp:cap:remove')")
    @PostMapping("/removeCap")
    @Operation(summary = "删除能力包")
    public Result<String> removeCap(@RequestBody @Valid CommonIdDto dto) throws Exception {
        capService.removeCap(dto);
        return Result.success("操作成功");
    }

}
