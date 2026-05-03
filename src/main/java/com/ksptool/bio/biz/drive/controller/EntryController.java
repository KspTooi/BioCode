package com.ksptool.bio.biz.drive.controller;


import com.ksptool.assembly.entity.web.Result;
import com.ksptool.bio.biz.drive.common.aop.DriveSpace;
import com.ksptool.bio.biz.drive.model.driveentry.dto.*;
import com.ksptool.bio.biz.drive.model.driveentry.vo.CheckEntryMoveVo;
import com.ksptool.bio.biz.drive.model.driveentry.vo.GetDriveInfo;
import com.ksptool.bio.biz.drive.model.driveentry.vo.GetEntryDetailsVo;
import com.ksptool.bio.biz.drive.model.driveentry.vo.GetEntryListVo;
import com.ksptool.bio.biz.drive.service.DriveSpaceService;
import com.ksptool.bio.biz.drive.service.EntryService;
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

import static com.ksptool.bio.biz.auth.service.SessionService.hasSuperCode;

@PrintLog
@RestController
@RequestMapping("/drive/entry")
@Tag(name = "DRIVE-云盘条目管理", description = "云盘条目管理")
@Slf4j
public class EntryController {

    @Autowired
    private EntryService entryService;

    @Autowired
    private DriveSpaceService driveSpaceService;

    @DriveSpace
    @PreAuthorize("@auth.hasCode('drive:entry:view')")
    @PostMapping("/getDriveInfo")
    @Operation(summary = "获取云盘信息")
    public Result<GetDriveInfo> getDriveInfo(@RequestBody @Valid DriveCommonIdDto dto) throws Exception {
        return Result.success(entryService.getDriveInfo());
    }

    @DriveSpace
    @PreAuthorize("@auth.hasCode('drive:entry:view')")
    @PostMapping("/getEntryList")
    @Operation(summary = "查询条目列表")
    public Result<GetEntryListVo> getEntryList(@RequestBody @Valid GetEntryListDto dto) throws Exception {

        //如果没有超级管理员权限,需要查一下我在云盘空间里面有没有角色
        if (!hasSuperCode()) {

            var myBestRole = driveSpaceService.getMyBestRole(dto.getDriveSpaceId());

            if (myBestRole == null) {
                return Result.error("你没有权限访问这个云盘空间");
            }

        }

        var ret = entryService.getEntryList(dto);
        return Result.success(ret);
    }


    @DriveSpace
    @PreAuthorize("@auth.hasCode('drive:entry:add')")
    @Operation(summary = "新增条目")
    @PostMapping("/addEntry")
    public Result<String> addEntry(@RequestBody @Valid AddEntryDto dto) throws Exception {

        //验证输入参数
        if (dto.validate() != null) {
            return Result.error(dto.validate());
        }

        //如果没有超级管理员权限,需要查一下我在云盘空间里面有没有角色
        if (!hasSuperCode()) {

            var myBestRole = driveSpaceService.getMyBestRole(dto.getDriveSpaceId());

            if (myBestRole == null) {
                return Result.error("你没有权限访问这个云盘空间");
            }

            //只有主管理员、行政管理员、编辑者可以新增条目
            if (myBestRole != 0 && myBestRole != 1 && myBestRole != 2) {
                return Result.error("你没有权限新增条目");
            }

        }

        //新增条目
        entryService.addEntry(dto);
        return Result.success("新增成功");
    }

    @DriveSpace
    @PreAuthorize("@auth.hasCode('drive:entry:copy')")
    @Operation(summary = "复制条目")
    @PostMapping("/copyEntry")
    public Result<String> copyEntry(@RequestBody @Valid CopyEntryDto dto) throws Exception {

        //如果没有超级管理员权限,需要查一下我在云盘空间里面有没有角色
        if (!hasSuperCode()) {

            var myBestRole = driveSpaceService.getMyBestRole(dto.getDriveSpaceId());

            if (myBestRole == null) {
                return Result.error("你没有权限访问这个云盘空间");
            }

            //只有主管理员、行政管理员、编辑者可以复制条目
            if (myBestRole != 0 && myBestRole != 1 && myBestRole != 2) {
                return Result.error("你没有权限复制条目");
            }

        }

        entryService.copyEntry(dto);
        return Result.success("复制成功");
    }

    @DriveSpace
    @PreAuthorize("@auth.hasCode('drive:entry:rename')")
    @Operation(summary = "重命名条目")
    @PostMapping("/renameEntry")
    public Result<String> renameEntry(@RequestBody @Valid RenameEntry dto) throws Exception {

        //如果没有超级管理员权限,需要查一下我在云盘空间里面有没有角色
        if (!hasSuperCode()) {

            var myBestRole = driveSpaceService.getMyBestRole(dto.getDriveSpaceId());
            if (myBestRole == null) {
                return Result.error("你没有权限访问这个云盘空间");
            }

            //只有主管理员、行政管理员、编辑者可以重命名条目
            if (myBestRole != 0 && myBestRole != 1 && myBestRole != 2) {
                return Result.error("你没有权限重命名条目");
            }
        }

        entryService.renameEntry(dto);
        return Result.success("重命名成功");
    }

    @DriveSpace
    @PreAuthorize("@auth.hasCode('drive:entry:move')")
    @Operation(summary = "移动检测", description = "用于移动条目前的检测")
    @PostMapping("/checkEntryMove")
    public Result<CheckEntryMoveVo> checkEntryMove(@RequestBody @Valid MoveEntryDto dto) throws Exception {

        //如果没有超级管理员权限,需要查一下我在云盘空间里面有没有角色
        if (!hasSuperCode()) {
            var myBestRole = driveSpaceService.getMyBestRole(dto.getDriveSpaceId());
            if (myBestRole == null) {
                return Result.error("你没有权限访问这个云盘空间");
            }

            //只有主管理员、行政管理员、编辑者可以移动条目
            if (myBestRole != 0 && myBestRole != 1 && myBestRole != 2) {
                return Result.error("你没有权限移动条目");
            }
        }

        var ret = entryService.checkEntryMove(dto);
        return Result.success(ret);
    }

    @DriveSpace
    @PreAuthorize("@auth.hasCode('drive:entry:move')")
    @Operation(summary = "移动条目", description = "允许用户在云盘内移动条目")
    @PostMapping("/moveEntry")
    public Result<String> moveEntry(@RequestBody @Valid MoveEntryDto dto) throws Exception {

        //如果没有超级管理员权限,需要查一下我在云盘空间里面有没有角色
        if (!hasSuperCode()) {

            var myBestRole = driveSpaceService.getMyBestRole(dto.getDriveSpaceId());

            if (myBestRole == null) {
                return Result.error("你没有权限访问这个云盘空间");
            }

            //只有主管理员、行政管理员、编辑者可以移动条目
            if (myBestRole != 0 && myBestRole != 1 && myBestRole != 2) {
                return Result.error("你没有权限移动条目");
            }

        }

        entryService.moveEntry(dto);
        return Result.success("移动成功");
    }

    @DriveSpace
    @PreAuthorize("@auth.hasCode('drive:entry:view')")
    @Operation(summary = "查询条目详情")
    @PostMapping("/getEntryDetails")
    public Result<GetEntryDetailsVo> getEntryDetails(@RequestBody @Valid DriveCommonIdDto dto) throws Exception {

        //如果没有超级管理员权限,需要查一下我在云盘空间里面有没有角色
        if (!hasSuperCode()) {
            var myBestRole = driveSpaceService.getMyBestRole(dto.getDriveSpaceId());
            if (myBestRole == null) {
                return Result.error("你没有权限访问这个云盘空间");
            }
        }


        GetEntryDetailsVo details = entryService.getEntryDetails(dto);
        if (details == null) {
            return Result.error("无数据");
        }

        return Result.success(details);
    }

    @DriveSpace
    @PreAuthorize("@auth.hasCode('drive:entry:remove')")
    @Operation(summary = "删除条目")
    @PostMapping("/removeEntry")
    public Result<String> removeEntry(@RequestBody @Valid DriveCommonIdDto dto) throws Exception {

        //如果没有超级管理员权限,需要查一下我在云盘空间里面有没有角色
        if (!hasSuperCode()) {
            var myBestRole = driveSpaceService.getMyBestRole(dto.getDriveSpaceId());
            if (myBestRole == null) {
                return Result.error("你没有权限访问这个云盘空间");
            }
            //只有主管理员、行政管理员、编辑者可以删除条目
            if (myBestRole != 0 && myBestRole != 1 && myBestRole != 2) {
                return Result.error("你没有权限删除条目");
            }
        }

        entryService.removeEntry(dto);
        return Result.success("操作成功");
    }
}
