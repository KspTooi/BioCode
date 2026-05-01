package com.ksptool.bio.biz.auth.controller;

import com.ksptool.assembly.entity.exception.AuthException;
import com.ksptool.assembly.entity.exception.BizException;
import com.ksptool.assembly.entity.web.Result;
import com.ksptool.bio.biz.auth.model.profile.dto.ChangePasswordDto;
import com.ksptool.bio.biz.auth.model.profile.vo.GetCurrentUserProfileVo;
import com.ksptool.bio.biz.auth.service.UserProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import static com.ksptool.bio.biz.auth.service.SessionService.session;

@Tag(name = "AUTH-用户档案管理", description = "用户档案管理")
@RestController
@RequestMapping("/profile")
public class UserProfileController {

    @Autowired
    private UserProfileService profileService;

    @Operation(summary = "获取当前用户信息")
    @PostMapping("/getCurrentUserProfile")
    @ResponseBody
    public Result<GetCurrentUserProfileVo> getCurrentUserProfile() throws AuthException {
        return Result.success(profileService.getUserProfile(session().getUserId()));
    }

    @Operation(summary = "获取当前用户头像")
    @GetMapping("/getUserAvatar")
    public ResponseEntity<Resource> getUserAvatar() throws AuthException {
        return profileService.getUserAvatar();
    }

    @Operation(summary = "更新当前用户头像")
    @PostMapping("/updateUserAvatar")
    public ResponseEntity<Resource> updateUserAvatar(@RequestParam("file") MultipartFile file) throws AuthException {
        return profileService.updateUserAvatar(file);
    }

    @Operation(summary = "用户更改密码")
    @PostMapping("/changePassword")
    public Result<String> changePassword(@RequestBody @Valid ChangePasswordDto dto) throws BizException, AuthException {
        profileService.changePassword(dto);
        return Result.success("密码修改成功");
    }

}
