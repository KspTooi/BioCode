package com.ksptool.bio.biz.drive.controller;

import com.ksptool.assembly.entity.web.CommonIdDto;
import com.ksptool.assembly.entity.web.PageResult;
import com.ksptool.assembly.entity.web.Result;
import com.ksptool.bio.biz.drive.model.drivespace.dto.AddDriveSpaceDto;
import com.ksptool.bio.biz.drive.model.drivespace.dto.EditDriveSpaceDto;
import com.ksptool.bio.biz.drive.model.drivespace.dto.EditDriveSpaceMembersDto;
import com.ksptool.bio.biz.drive.model.drivespace.dto.GetDriveSpaceListDto;
import com.ksptool.bio.biz.drive.model.drivespace.vo.GetDriveSpaceDetailsVo;
import com.ksptool.bio.biz.drive.model.drivespace.vo.GetDriveSpaceListVo;
import com.ksptool.bio.biz.drive.service.DriveSpaceService;
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


@RestController
@RequestMapping("/driveSpace")
@Tag(name = "DRIVE-云盘空间管理", description = "云盘空间管理")
@Slf4j
public class DriveSpaceController {

    @Autowired
    private DriveSpaceService driveSpaceService;

    @PreAuthorize("@auth.hasCode('drive:space:view')")
    @PostMapping("/getDriveSpaceList")
    @Operation(summary = "查询云盘空间列表")
    public PageResult<GetDriveSpaceListVo> getDriveSpaceList(@RequestBody @Valid GetDriveSpaceListDto dto) throws Exception {
        return driveSpaceService.getDriveSpaceList(dto);
    }

    @PreAuthorize("@auth.hasCode('drive:space:add')")
    @Operation(summary = "新增云盘空间")
    @PostMapping("/addDriveSpace")
    public Result<String> addDriveSpace(@RequestBody @Valid AddDriveSpaceDto dto) throws Exception {
        driveSpaceService.addDriveSpace(dto);
        return Result.success("新增成功");
    }

    @PreAuthorize("@auth.hasCode('drive:space:edit')")
    @Operation(summary = "编辑云盘空间")
    @PostMapping("/editDriveSpace")
    public Result<String> editDriveSpace(@RequestBody @Valid EditDriveSpaceDto dto) throws Exception {
        driveSpaceService.editDriveSpace(dto);
        return Result.success("修改成功");
    }

    @PreAuthorize("@auth.hasCode('drive:space:edit')")
    @Operation(summary = "编辑云盘空间成员")
    @PostMapping("/editDriveSpaceMembers")
    public Result<String> editDriveSpaceMembers(@RequestBody @Valid EditDriveSpaceMembersDto dto) throws Exception {
        driveSpaceService.editDriveSpaceMembers(dto);
        return Result.success("操作成功");
    }


    @PreAuthorize("@auth.hasCode('drive:space:view')")
    @Operation(summary = "查询云盘空间详情")
    @PostMapping("/getDriveSpaceDetails")
    public Result<GetDriveSpaceDetailsVo> getDriveSpaceDetails(@RequestBody @Valid CommonIdDto dto) throws Exception {
        GetDriveSpaceDetailsVo details = driveSpaceService.getDriveSpaceDetails(dto);
        if (details == null) {
            return Result.error("无数据");
        }
        return Result.success(details);
    }

    @PreAuthorize("@auth.hasCode('drive:space:remove')")
    @Operation(summary = "删除云盘空间")
    @PostMapping("/removeDriveSpace")
    public Result<String> removeDriveSpace(@RequestBody @Valid CommonIdDto dto) throws Exception {
        driveSpaceService.removeDriveSpace(dto);
        return Result.success("操作成功");
    }

}
