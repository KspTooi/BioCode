package com.ksptool.bio.biz.assembly.controller;

import com.ksptool.assembly.entity.web.CommonIdDto;
import com.ksptool.assembly.entity.web.PageResult;
import com.ksptool.assembly.entity.web.Result;
import com.ksptool.bio.biz.assembly.model.polymodel.dto.AddPolyModelDto;
import com.ksptool.bio.biz.assembly.model.polymodel.dto.EditPolyModelDto;
import com.ksptool.bio.biz.assembly.model.polymodel.dto.GetPolyModelListDto;
import com.ksptool.bio.biz.assembly.model.polymodel.vo.GetPolyModelDetailsVo;
import com.ksptool.bio.biz.assembly.model.polymodel.vo.GetPolyModelListVo;
import com.ksptool.bio.biz.assembly.service.PolyModelService;
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
@RequestMapping("/polyModel")
@Tag(name = "ASSEMBLY-输出方案聚合模型管理", description = "输出方案聚合模型管理")
@Slf4j
public class
PolyModelController {

    @Autowired
    private PolyModelService polyModelService;

    @PostMapping("/getPolyModelList")
    @Operation(summary = "查询聚合模型列表")
    public PageResult<GetPolyModelListVo> getPolyModelList(@RequestBody @Valid GetPolyModelListDto dto) throws Exception {
        return polyModelService.getPolyModelList(dto);
    }

    @Operation(summary = "新增聚合模型")
    @PostMapping("/addPolyModel")
    public Result<String> addPolyModel(@RequestBody @Valid AddPolyModelDto dto) throws Exception {
        polyModelService.addPolyModel(dto);
        return Result.success("新增成功");
    }

    @Operation(summary = "编辑聚合模型")
    @PostMapping("/editPolyModel")
    public Result<String> editPolyModel(@RequestBody @Valid EditPolyModelDto dto) throws Exception {
        polyModelService.editPolyModel(dto);
        return Result.success("修改成功");
    }

    @Operation(summary = "查询聚合模型详情")
    @PostMapping("/getPolyModelDetails")
    public Result<GetPolyModelDetailsVo> getPolyModelDetails(@RequestBody @Valid CommonIdDto dto) throws Exception {
        GetPolyModelDetailsVo details = polyModelService.getPolyModelDetails(dto);
        if (details == null) {
            return Result.error("无数据");
        }
        return Result.success(details);
    }

    @Operation(summary = "删除聚合模型元素")
    @PostMapping("/removePolyModel")
    public Result<String> removePolyModel(@RequestBody @Valid CommonIdDto dto) throws Exception {
        polyModelService.removePolyModel(dto);
        return Result.success("操作成功");
    }

    @Operation(summary = "从原始模型同步聚合模型(输出方案ID)")
    @PostMapping("/importFromRaw")
    public Result<String> importFromRaw(@RequestBody @Valid CommonIdDto dto) throws Exception {
        polyModelService.importFromRaw(dto);
        return Result.success("同步成功");
    }

}
