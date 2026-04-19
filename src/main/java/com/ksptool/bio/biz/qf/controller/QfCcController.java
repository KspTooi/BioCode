package com.ksptool.bio.biz.qf.controller;

import com.ksptool.assembly.entity.web.CommonIdDto;
import com.ksptool.assembly.entity.web.PageResult;
import com.ksptool.assembly.entity.web.Result;
import com.ksptool.bio.biz.qf.model.qfcc.dto.AddQfCcDto;
import com.ksptool.bio.biz.qf.model.qfcc.dto.EditQfCcDto;
import com.ksptool.bio.biz.qf.model.qfcc.dto.GetQfCcListDto;
import com.ksptool.bio.biz.qf.model.qfcc.vo.GetQfCcDetailsVo;
import com.ksptool.bio.biz.qf.model.qfcc.vo.GetQfCcListVo;
import com.ksptool.bio.biz.qf.service.QfCcService;
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

/**
 * 抄送
 *
 * @author Akkarin(1075613357@qq.com)
 * @author KspTool(ksptool@outlook.com)
 * @license Proprietary
 * 版权所有 (c) 2026 KspTool及其贡献者保留所有权利。
 * 未经事先书面许可，严禁任何形式的复制或分发。
 * @since 2026-04-17
 */
@PrintLog
@RestController
@RequestMapping("/qfCc")
@Tag(name = "抄送", description = "抄送")
@Slf4j
public class QfCcController {

    @Autowired
    private QfCcService qfCcService;

    @PreAuthorize("@auth.hasCode('qf:cc:view')")
    @PostMapping("/getQfCcList")
    @Operation(summary = "查询抄送列表")
    public PageResult<GetQfCcListVo> getQfCcList(@RequestBody @Valid GetQfCcListDto dto) throws Exception {
        return qfCcService.getQfCcList(dto);
    }


    @PreAuthorize("@auth.hasCode('qf:cc:view')")
    @Operation(summary = "查询抄送详情")
    @PostMapping("/getQfCcDetails")
    public Result<GetQfCcDetailsVo> getQfCcDetails(@RequestBody @Valid CommonIdDto dto) throws Exception {
        GetQfCcDetailsVo details = qfCcService.getQfCcDetails(dto);
        if (details == null) {
            return Result.error("无数据");
        }
        return Result.success(details);
    }

    @PreAuthorize("@auth.hasCode('qf:cc:remove')")
    @Operation(summary = "删除抄送")
    @PostMapping("/removeQfCc")
    public Result<String> removeQfCc(@RequestBody @Valid CommonIdDto dto) throws Exception {
        qfCcService.removeQfCc(dto);
        return Result.success("操作成功");
    }

}
