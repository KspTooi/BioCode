package com.ksptool.bio.biz.core.controller;

import com.ksptool.bio.biz.core.model.exceltemplate.dto.EditExcelTemplateDto;
import com.ksptool.bio.biz.core.model.exceltemplate.dto.GetExcelTemplateListDto;
import com.ksptool.bio.biz.core.model.exceltemplate.vo.GetExcelTemplateListVo;
import com.ksptool.bio.biz.core.service.ExcelTemplateService;
import com.ksptool.assembly.entity.web.CommonIdDto;
import com.ksptool.assembly.entity.web.PageResult;
import com.ksptool.assembly.entity.web.Result;
import com.ksptool.bio.commons.annotation.PrintLog;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Objects;


@PrintLog
@RestController
@RequestMapping("/excelTemplate")
@Tag(name = "CORE-Excel导入模板管理", description = "Excel导入模板管理")
@Slf4j
public class ExcelTemplateController {

    @Autowired
    private ExcelTemplateService excelTemplateService;

    @PreAuthorize("@auth.hasCode('core:excel_template:list')")
    @PostMapping("/getExcelTemplateList")
    @Operation(summary = "查询Excel模板列表")
    public PageResult<GetExcelTemplateListVo> getExcelTemplateList(@RequestBody @Valid GetExcelTemplateListDto dto) throws Exception {
        return excelTemplateService.getExcelTemplateList(dto);
    }

    @PreAuthorize("@auth.hasCode('core:excel_template:edit')")
    @Operation(summary = "编辑Excel模板")
    @PostMapping("/editExcelTemplate")
    public Result<String> editExcelTemplate(@RequestBody @Valid EditExcelTemplateDto dto) throws Exception {
        excelTemplateService.editExcelTemplate(dto);
        return Result.success("修改成功");
    }


    @PreAuthorize("@auth.hasCode('core:excel_template:remove')")
    @Operation(summary = "删除Excel模板")
    @PostMapping("/removeExcelTemplate")
    public Result<String> removeExcelTemplate(@RequestBody @Valid CommonIdDto dto) throws Exception {
        excelTemplateService.removeExcelTemplate(dto);
        return Result.success("操作成功");
    }


    @PreAuthorize("@auth.hasCode('core:excel_template:upload')")
    @Operation(summary = "上传Excel模板")
    @PostMapping("/uploadExcelTemplate")
    public Result<String> uploadExcelTemplate(@RequestParam("file") MultipartFile[] file) throws Exception {

        if (file == null || file.length == 0) {
            return Result.error("文件不能为空");
        }

        //最多上传50个模板
        if (file.length > 50) {
            return Result.error("最多上传50个模板");
        }

        for (MultipartFile f : file) {
            if (f.isEmpty()) {
                return Result.error("文件不能为空");
            }
            //只能上传xlsx文件
            if (!Objects.requireNonNull(f.getOriginalFilename()).endsWith(".xlsx")) {
                return Result.error("只能上传xlsx文件");
            }
            //只能且必须有一个-
            if (!f.getOriginalFilename().contains("-")) {
                return Result.error("文件名必须包含-");
            }
            //-前面为模板名称
            var name = f.getOriginalFilename().split("-")[0];
            if (name == null) {
                return Result.error("模板名称不能为空");
            }
            //-后面为唯一标识符
            var key = f.getOriginalFilename().split("-")[1];
            if (key == null) {
                return Result.error("唯一标识符不能为空");
            }
            //唯一标识符只能包含字母、数字和下划线
            if (!key.matches("[a-zA-Z0-9_]+\\.xlsx")) {
                return Result.error("唯一标识符只能包含字母、数字和下划线");
            }
            //不能多个-
            if (f.getOriginalFilename().split("-").length > 2) {
                return Result.error("文件名不能有多个-");
            }
        }

        //处理上传
        excelTemplateService.uploadExcelTemplate(file);
        return Result.success("上传成功");
    }

    @PreAuthorize("@auth.hasCode('core:excel_template:download')")
    @Operation(summary = "下载Excel模板")
    @RequestMapping("/downloadExcelTemplate")
    public void downloadExcelTemplate(@RequestParam("code") String code, HttpServletResponse response) throws Exception {
        excelTemplateService.downloadExcelTemplate(code, response);
    }

}
