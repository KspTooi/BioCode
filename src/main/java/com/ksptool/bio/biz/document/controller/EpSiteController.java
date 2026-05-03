package com.ksptool.bio.biz.document.controller;

import com.ksptool.assembly.entity.web.CommonIdDto;
import com.ksptool.assembly.entity.web.PageResult;
import com.ksptool.assembly.entity.web.Result;
import com.ksptool.bio.biz.document.model.epsite.dto.AddEpSiteDto;
import com.ksptool.bio.biz.document.model.epsite.dto.EditEpSiteDto;
import com.ksptool.bio.biz.document.model.epsite.dto.GetEpSiteListDto;
import com.ksptool.bio.biz.document.model.epsite.dto.ImportEpSiteDto;
import com.ksptool.bio.biz.document.model.epsite.vo.ExportEpSiteVo;
import com.ksptool.bio.biz.document.model.epsite.vo.GetEpSiteDetailsVo;
import com.ksptool.bio.biz.document.model.epsite.vo.GetEpSiteListVo;
import com.ksptool.bio.biz.document.service.EpSiteService;
import com.ksptool.bio.commons.annotation.PrintLog;
import com.ksptool.bio.commons.dataprocess.ExportWizard;
import com.ksptool.bio.commons.dataprocess.ImportWizard;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@PrintLog
@RestController
@RequestMapping("/epSite")
@Tag(name = "EP-站点管理", description = "站点管理")
@Slf4j
public class EpSiteController {

    @Autowired
    private EpSiteService epSiteService;

    @PreAuthorize("@auth.hasCode('ep:site:view')")
    @PostMapping("/getEpSiteList")
    @Operation(summary = "查询站点列表")
    public PageResult<GetEpSiteListVo> getEpSiteList(@RequestBody @Valid GetEpSiteListDto dto) throws Exception {
        return epSiteService.getEpSiteList(dto);
    }

    @PreAuthorize("@auth.hasCode('ep:site:add')")
    @Operation(summary = "新增站点")
    @PostMapping("/addEpSite")
    public Result<String> addEpSite(@RequestBody @Valid AddEpSiteDto dto) throws Exception {
        epSiteService.addEpSite(dto);
        return Result.success("新增成功");
    }

    @PreAuthorize("@auth.hasCode('ep:site:edit')")
    @Operation(summary = "编辑站点")
    @PostMapping("/editEpSite")
    public Result<String> editEpSite(@RequestBody @Valid EditEpSiteDto dto) throws Exception {
        epSiteService.editEpSite(dto);
        return Result.success("修改成功");
    }

    @PreAuthorize("@auth.hasCode('ep:site:view')")
    @Operation(summary = "查询站点详情")
    @PostMapping("/getEpSiteDetails")
    public Result<GetEpSiteDetailsVo> getEpSiteDetails(@RequestBody @Valid CommonIdDto dto) throws Exception {
        GetEpSiteDetailsVo details = epSiteService.getEpSiteDetails(dto);
        if (details == null) {
            return Result.error("无数据");
        }
        return Result.success(details);
    }

    @PreAuthorize("@auth.hasCode('ep:site:remove')")
    @Operation(summary = "删除站点")
    @PostMapping("/removeEpSite")
    public Result<String> removeEpSite(@RequestBody @Valid CommonIdDto dto) throws Exception {
        epSiteService.removeEpSite(dto);
        return Result.success("操作成功");
    }

    @PreAuthorize("@auth.hasCode('ep:site:import')")
    @Operation(summary = "导入站点")
    @PostMapping("/importEpSite")
    public Result<String> importEpSite(@RequestParam("file") MultipartFile file) throws Exception {

        var iw = new ImportWizard<>(file, ImportEpSiteDto.class);

        iw.transfer();

        var errors = iw.validate();

        if (StringUtils.isNotBlank(errors)) {
            return Result.error(errors);
        }

        var data = iw.getData();
        var count = epSiteService.importEpSite(data);
        return Result.success("操作成功,已导入数据:" + count + "条", null);
    }

    @PreAuthorize("@auth.hasCode('ep:site:export')")
    @Operation(summary = "导出站点")
    @RequestMapping("/exportEpSite")
    public void exportEpSite(@RequestBody @Valid GetEpSiteListDto dto, HttpServletResponse hsrp) throws Exception {
        var ew = new ExportWizard<ExportEpSiteVo>(epSiteService.exportEpSite(dto), hsrp);
        ew.transfer("站点");
    }

}
