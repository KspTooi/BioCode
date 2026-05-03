package com.ksptool.bio.biz.assembly.controller;

import com.ksptool.assembly.entity.web.CommonIdDto;
import com.ksptool.assembly.entity.web.PageResult;
import com.ksptool.assembly.entity.web.Result;
import com.ksptool.bio.biz.assembly.model.tymschemafield.dto.AddTymSchemaFieldDto;
import com.ksptool.bio.biz.assembly.model.tymschemafield.dto.EditTymSchemaFieldDto;
import com.ksptool.bio.biz.assembly.model.tymschemafield.dto.GetTymSchemaFieldListDto;
import com.ksptool.bio.biz.assembly.model.tymschemafield.vo.GetTymSchemaFieldDetailsVo;
import com.ksptool.bio.biz.assembly.model.tymschemafield.vo.GetTymSchemaFieldListVo;
import com.ksptool.bio.biz.assembly.service.TymSchemaFieldService;
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
@RequestMapping("/tymSchemaField")
@Tag(name = "ASSEMBLY-类型映射方案字段管理", description = "类型映射方案字段管理")
@Slf4j
public class TymSchemaFieldController {

    @Autowired
    private TymSchemaFieldService tymSchemaFieldService;

    @PostMapping("/getTymSchemaFieldList")
    @Operation(summary = "查询类型映射方案字段列表")
    public PageResult<GetTymSchemaFieldListVo> getTymSchemaFieldList(@RequestBody @Valid GetTymSchemaFieldListDto dto) throws Exception {
        return tymSchemaFieldService.getTymSchemaFieldList(dto);
    }

    @Operation(summary = "新增类型映射方案字段")
    @PostMapping("/addTymSchemaField")
    public Result<String> addTymSchemaField(@RequestBody @Valid AddTymSchemaFieldDto dto) throws Exception {
        tymSchemaFieldService.addTymSchemaField(dto);
        return Result.success("新增成功");
    }

    @Operation(summary = "编辑类型映射方案字段")
    @PostMapping("/editTymSchemaField")
    public Result<String> editTymSchemaField(@RequestBody @Valid EditTymSchemaFieldDto dto) throws Exception {
        tymSchemaFieldService.editTymSchemaField(dto);
        return Result.success("修改成功");
    }

    @Operation(summary = "查询类型映射方案字段详情")
    @PostMapping("/getTymSchemaFieldDetails")
    public Result<GetTymSchemaFieldDetailsVo> getTymSchemaFieldDetails(@RequestBody @Valid CommonIdDto dto) throws Exception {
        GetTymSchemaFieldDetailsVo details = tymSchemaFieldService.getTymSchemaFieldDetails(dto);
        if (details == null) {
            return Result.error("无数据");
        }
        return Result.success(details);
    }

    @Operation(summary = "删除类型映射方案字段")
    @PostMapping("/removeTymSchemaField")
    public Result<String> removeTymSchemaField(@RequestBody @Valid CommonIdDto dto) throws Exception {
        tymSchemaFieldService.removeTymSchemaField(dto);
        return Result.success("操作成功");
    }

}
