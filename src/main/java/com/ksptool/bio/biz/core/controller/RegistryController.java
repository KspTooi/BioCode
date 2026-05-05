package com.ksptool.bio.biz.core.controller;

import com.ksptool.bio.biz.core.model.registry.dto.AddRegistryDto;
import com.ksptool.bio.biz.core.model.registry.dto.EditRegistryDto;
import com.ksptool.bio.biz.core.model.registry.dto.GetRegistryListDto;
import com.ksptool.bio.biz.core.model.registry.dto.ImportRegistryDto;
import com.ksptool.bio.biz.core.model.registry.vo.ExportRegistryVo;
import com.ksptool.bio.biz.core.model.registry.vo.GetRegistryDetailsVo;
import com.ksptool.bio.biz.core.model.registry.vo.GetRegistryEntryListVo;
import com.ksptool.bio.biz.core.model.registry.vo.GetRegistryNodeTreeVo;
import com.ksptool.bio.biz.core.service.RegistrySdk;
import com.ksptool.bio.biz.core.service.RegistryService;
import com.ksptool.bio.biz.auth.service.SessionService;
import com.ksptool.assembly.entity.exception.BizException;
import com.ksptool.assembly.entity.web.CommonIdDto;
import com.ksptool.assembly.entity.web.PageResult;
import com.ksptool.assembly.entity.web.Result;
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

import java.util.List;

@RestController
@RequestMapping("/registry")
@Tag(name = "CORE-注册表管理", description = "注册表管理")
@Slf4j
public class RegistryController {

    @Autowired
    private RegistryService registryService;

    @Autowired
    private RegistrySdk registrySdk;

    @PreAuthorize("@auth.hasCode('core:registry:list')")
    @PostMapping("/getRegistryNodeTree")
    @Operation(summary = "查询注册表节点树")
    public Result<List<GetRegistryNodeTreeVo>> getRegistryNodeTree() throws Exception {
        return Result.success(registryService.getRegistryNodeTree());
    }

    @PreAuthorize("@auth.hasCode('core:registry:list')")
    @PostMapping("/getRegistryEntryList")
    @Operation(summary = "查询注册表条目列表")
    public PageResult<GetRegistryEntryListVo> getRegistryEntryList(@RequestBody @Valid GetRegistryListDto dto) throws Exception {
        return registryService.getRegistryEntryList(dto);
    }

    @PreAuthorize("@auth.hasCode('core:registry:add')")
    @Operation(summary = "新增注册表条目")
    @PostMapping("/addRegistry")
    public Result<String> addRegistry(@RequestBody @Valid AddRegistryDto dto) throws Exception {

        //验证输入参数
        String validate = dto.validate();
        if (StringUtils.isNotBlank(validate)) {
            return Result.error(validate);
        }

        registryService.addRegistry(dto);
        return Result.success("新增成功");
    }

    @PreAuthorize("@auth.hasCode('core:registry:edit')")
    @Operation(summary = "编辑注册表条目")
    @PostMapping("/editRegistry")
    public Result<String> editRegistry(@RequestBody @Valid EditRegistryDto dto) throws Exception {

        //验证输入参数
        String validate = dto.validate();
        if (StringUtils.isNotBlank(validate)) {
            return Result.error(validate);
        }

        registryService.editRegistry(dto);
        return Result.success("修改成功");
    }

    @PreAuthorize("@auth.hasCode('core:registry:details')")
    @Operation(summary = "查询注册表条目详情")
    @PostMapping("/getRegistryDetails")
    public Result<GetRegistryDetailsVo> getRegistryDetails(@RequestBody @Valid CommonIdDto dto) throws Exception {
        GetRegistryDetailsVo details = registryService.getRegistryDetails(dto);
        if (details == null) {
            return Result.error("无数据");
        }
        return Result.success(details);
    }

    @PreAuthorize("@auth.hasCode('core:registry:remove')")
    @Operation(summary = "删除注册表条目")
    @PostMapping("/removeRegistry")
    public Result<String> removeRegistry(@RequestBody @Valid CommonIdDto dto) throws Exception {
        registryService.removeRegistry(dto);
        return Result.success("操作成功");
    }

    @PreAuthorize("@auth.hasCode('core:registry:import')")
    @Operation(summary = "导入注册表条目")
    @PostMapping("/importRegistry")
    public Result<String> importRegistry(@RequestParam("file") MultipartFile file, @RequestParam("keyPath") String keyPath) throws Exception {

        //必填KeyPath
        if (StringUtils.isBlank(keyPath)) {
            return Result.error("KeyPath不能为空");
        }

        //准备向导
        ImportWizard<ImportRegistryDto> iw = new ImportWizard<>(file, ImportRegistryDto.class);

        //开始传输
        iw.transfer();

        //验证导入数据
        var errors = iw.validate();
        if (StringUtils.isNotBlank(errors)) {
            return Result.error(errors);
        }

        //获取导入数据
        var data = iw.getData();
        var count = registryService.importRegistry(keyPath, data);
        return Result.success("导入成功,已导入数据:" + count + "条", null);
    }

    @PreAuthorize("@auth.hasCode('core:registry:export')")
    @Operation(summary = "导出注册表条目", operationId = "registry:export")
    @RequestMapping("/exportRegistry")
    public void exportRegistry(@RequestBody @Valid GetRegistryListDto dto, HttpServletResponse response) throws Exception {

        //准备导出向导
        ExportWizard<ExportRegistryVo> ew = new ExportWizard<>(registryService.exportRegistry(dto), response);
        ew.transfer("注册表条目");
    }

    @PreAuthorize("@auth.hasCode('core:registry:clearcache')")
    @Operation(summary = "清除注册表缓存")
    @PostMapping("/clearRegistryCache")
    public Result<String> clearRegistryCache() throws BizException {
        registrySdk.clearAllCache();
        return Result.success("注册表缓存已清除");
    }

}
