package com.ksptool.bio.biz.core.controller;

import com.ksptool.assembly.entity.web.CommonIdDto;
import com.ksptool.assembly.entity.web.PageResult;
import com.ksptool.assembly.entity.web.Result;
import com.ksptool.bio.biz.core.model.pack.dto.AddPackDto;
import com.ksptool.bio.biz.core.model.pack.dto.EditPackDto;
import com.ksptool.bio.biz.core.model.pack.dto.GetPackListDto;
import com.ksptool.bio.biz.core.model.pack.dto.UpdatePackMenuDto;
import com.ksptool.bio.biz.core.model.pack.vo.GetPackDetailsVo;
import com.ksptool.bio.biz.core.model.pack.vo.GetPackListVo;
import com.ksptool.bio.biz.core.service.PackService;
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

import java.util.List;

@PrintLog
@RestController
@RequestMapping("/pack")
@Tag(name = "菜单包", description = "菜单包")
@Slf4j
public class PackController {

    @Autowired
    private PackService packService;

    @PreAuthorize("@auth.hasCode('core:pack:list')")
    @PostMapping("/getPackList")
    @Operation(summary = "查询菜单包列表")
    public PageResult<GetPackListVo> getPackList(@RequestBody @Valid GetPackListDto dto) throws Exception {
        return packService.getPackList(dto);
    }

    @PreAuthorize("@auth.hasCode('core:pack:add')")
    @Operation(summary = "新增菜单包")
    @PostMapping("/addPack")
    public Result<String> addPack(@RequestBody @Valid AddPackDto dto) throws Exception {
        packService.addPack(dto);
        return Result.success("新增成功");
    }

    @PreAuthorize("@auth.hasCode('core:pack:edit')")
    @Operation(summary = "编辑菜单包")
    @PostMapping("/editPack")
    public Result<String> editPack(@RequestBody @Valid EditPackDto dto) throws Exception {
        packService.editPack(dto);
        return Result.success("修改成功");
    }

    @PreAuthorize("@auth.hasCode('core:pack:details')")
    @Operation(summary = "查询菜单包详情")
    @PostMapping("/getPackDetails")
    public Result<GetPackDetailsVo> getPackDetails(@RequestBody @Valid CommonIdDto dto) throws Exception {
        GetPackDetailsVo details = packService.getPackDetails(dto);
        if (details == null) {
            return Result.error("无数据");
        }
        return Result.success(details);
    }

    @PreAuthorize("@auth.hasCode('core:pack:remove')")
    @Operation(summary = "删除菜单包")
    @PostMapping("/removePack")
    public Result<String> removePack(@RequestBody @Valid CommonIdDto dto) throws Exception {
        packService.removePack(dto);
        return Result.success("操作成功");
    }

    @PreAuthorize("@auth.hasCode('core:pack:list')")
    @Operation(summary = "根据菜单ID查询所属菜单包")
    @PostMapping("/getPacksByMenuId")
    public Result<List<GetPackListVo>> getPacksByMenuId(@RequestBody @Valid CommonIdDto dto) throws Exception {
        return Result.success(packService.getPacksByMenuId(dto.getId()));
    }

    @PreAuthorize("@auth.hasCode('core:pack:edit')")
    @Operation(summary = "更新菜单包的菜单绑定")
    @PostMapping("/updatePackMenu")
    public Result<String> updatePackMenu(@RequestBody @Valid UpdatePackMenuDto dto) throws Exception {
        packService.updatePackMenu(dto);
        return Result.success("更新成功");
    }

}
