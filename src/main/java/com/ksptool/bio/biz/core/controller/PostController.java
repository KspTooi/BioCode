package com.ksptool.bio.biz.core.controller;

import com.ksptool.bio.biz.core.model.post.dto.AddPostDto;
import com.ksptool.bio.biz.core.model.post.dto.EditPostDto;
import com.ksptool.bio.biz.core.model.post.dto.GetPostListDto;
import com.ksptool.bio.biz.core.model.post.vo.GetPostDetailsVo;
import com.ksptool.bio.biz.core.model.post.vo.GetPostListVo;
import com.ksptool.bio.biz.core.service.PostService;
import com.ksptool.assembly.entity.web.CommonIdDto;
import com.ksptool.assembly.entity.web.PageResult;
import com.ksptool.assembly.entity.web.Result;
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


@PrintLog
@RestController
@RequestMapping("/post")
@Tag(name = "CORE-岗位管理", description = "岗位管理")
@Slf4j
public class PostController {

    @Autowired
    private PostService postService;

    @PreAuthorize("@auth.hasCode('core:post:view')")
    @PostMapping("/getPostList")
    @Operation(summary = "查询岗位列表")
    public PageResult<GetPostListVo> getPostList(@RequestBody @Valid GetPostListDto dto) throws Exception {
        return postService.getPostList(dto);
    }

    @PreAuthorize("@auth.hasCode('core:post:add')")
    @Operation(summary = "新增岗位")
    @PostMapping("/addPost")
    public Result<String> addPost(@RequestBody @Valid AddPostDto dto) throws Exception {
        postService.addPost(dto);
        return Result.success("新增成功");
    }

    @PreAuthorize("@auth.hasCode('core:post:edit')")
    @Operation(summary = "编辑岗位")
    @PostMapping("/editPost")
    public Result<String> editPost(@RequestBody @Valid EditPostDto dto) throws Exception {
        postService.editPost(dto);
        return Result.success("修改成功");
    }

    @PreAuthorize("@auth.hasCode('core:post:view')")
    @Operation(summary = "查询岗位详情")
    @PostMapping("/getPostDetails")
    public Result<GetPostDetailsVo> getPostDetails(@RequestBody @Valid CommonIdDto dto) throws Exception {
        GetPostDetailsVo details = postService.getPostDetails(dto);
        if (details == null) {
            return Result.error("无数据");
        }
        return Result.success(details);
    }

    @PreAuthorize("@auth.hasCode('core:post:remove')")
    @Operation(summary = "删除岗位")
    @PostMapping("/removePost")
    public Result<String> removePost(@RequestBody @Valid CommonIdDto dto) throws Exception {
        postService.removePost(dto);
        return Result.success("操作成功");
    }

}
