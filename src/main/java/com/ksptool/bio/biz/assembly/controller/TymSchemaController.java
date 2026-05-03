package com.ksptool.bio.biz.assembly.controller;

import com.ksptool.assembly.entity.exception.BizException;
import com.ksptool.assembly.entity.web.CommonIdDto;
import com.ksptool.assembly.entity.web.PageResult;
import com.ksptool.assembly.entity.web.Result;
import com.ksptool.bio.biz.assembly.model.tymschema.dto.AddTymSchemaDto;
import com.ksptool.bio.biz.assembly.model.tymschema.dto.EditTymSchemaDto;
import com.ksptool.bio.biz.assembly.model.tymschema.dto.GetTymSchemaListDto;
import com.ksptool.bio.biz.assembly.model.tymschema.vo.GetTymSchemaDetailsVo;
import com.ksptool.bio.biz.assembly.model.tymschema.vo.GetTymSchemaListVo;
import com.ksptool.bio.biz.assembly.service.TymSchemaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/tymSchema")
@Tag(name = "ASSEMBLY-类型映射方案管理", description = "类型映射方案管理")
@Slf4j
public class TymSchemaController {

    @Autowired
    private TymSchemaService tymSchemaService;

    @PostMapping("/getTymSchemaList")
    @Operation(summary = "查询类型映射方案列表")
    public PageResult<GetTymSchemaListVo> getTymSchemaList(@RequestBody @Valid GetTymSchemaListDto dto) throws Exception {
        return tymSchemaService.getTymSchemaList(dto);
    }

    @Operation(summary = "新增类型映射方案")
    @PostMapping("/addTymSchema")
    public Result<String> addTymSchema(@RequestBody @Valid AddTymSchemaDto dto) throws Exception {
        tymSchemaService.addTymSchema(dto);
        return Result.success("新增成功");
    }

    @Operation(summary = "编辑类型映射方案")
    @PostMapping("/editTymSchema")
    public Result<String> editTymSchema(@RequestBody @Valid EditTymSchemaDto dto) throws Exception {
        tymSchemaService.editTymSchema(dto);
        return Result.success("修改成功");
    }

    @Operation(summary = "查询类型映射方案详情")
    @PostMapping("/getTymSchemaDetails")
    public Result<GetTymSchemaDetailsVo> getTymSchemaDetails(@RequestBody @Valid CommonIdDto dto) throws Exception {
        GetTymSchemaDetailsVo details = tymSchemaService.getTymSchemaDetails(dto);
        if (details == null) {
            return Result.error("无数据");
        }
        return Result.success(details);
    }

    @Operation(summary = "删除类型映射方案")
    @PostMapping("/removeTymSchema")
    public Result<String> removeTymSchema(@RequestBody @Valid CommonIdDto dto) throws Exception {

        if (dto.isBatch()) {
            throw new BizException("不支持批量操作.");
        }
        
        tymSchemaService.removeTymSchema(dto);
        return Result.success("操作成功");
    }

}
