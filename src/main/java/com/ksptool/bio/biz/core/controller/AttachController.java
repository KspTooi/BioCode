package com.ksptool.bio.biz.core.controller;

import com.ksptool.assembly.entity.exception.BizException;
import com.ksptool.assembly.entity.web.Result;
import com.ksptool.bio.biz.core.model.attach.dto.PreCheckAttachDto;
import com.ksptool.bio.biz.core.model.attach.vo.ApplyChunkVo;
import com.ksptool.bio.biz.core.model.attach.vo.DirectUploadVo;
import com.ksptool.bio.biz.core.model.attach.vo.PreCheckAttachVo;
import com.ksptool.bio.biz.core.service.AttachService;
import com.ksptool.bio.commons.annotation.PrintLog;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@PrintLog
@RestController
@RequestMapping("/attach")
@Tag(name = "CORE-文件附件", description = "文件附件模块")
@Slf4j
public class AttachController {

    @Autowired
    private AttachService attachService;

    @PostMapping("/directUpload")
    @Operation(summary = "零散文件上传:上传附件")
    public Result<DirectUploadVo> directUpload(@RequestParam("file") MultipartFile file, @RequestParam("kind") String kind) throws BizException {

        if (file.isEmpty()) {
            throw new BizException("文件不能为空");
        }

        if (StringUtils.isBlank(kind)) {
            throw new BizException("业务代码不能为空");
        }

        return Result.success(attachService.directUpload(file, kind));
    }

    @GetMapping("/directPreview")
    @Operation(summary = "零散文件上传:预览附件")
    public ResponseEntity<Resource> directPreview(@RequestParam("path") String path) throws Exception {
        return attachService.directPreview(path);
    }


    @PostMapping("/preCheckAttach")
    @Operation(summary = "大文件上传:预检附件")
    public Result<PreCheckAttachVo> preCheckAttach(@RequestBody @Valid PreCheckAttachDto dto) throws BizException {
        var vo = attachService.preCheckAttach(dto);
        return Result.success(vo);
    }

    @PostMapping("/applyChunk")
    @Operation(summary = "大文件上传:应用区块")
    public Result<ApplyChunkVo> applyChunk(@RequestParam("file") MultipartFile file, @RequestParam("preCheckId") Long preCheckId, @RequestParam("chunkId") Long chunkId) throws BizException {

        if (file.isEmpty()) {
            throw new BizException("区块数据不能为空");
        }

        //不能超过6MB
        if (file.getSize() > 6 * 1024 * 1024) {
            throw new BizException("区块大小不能超过6MB");
        }

        if (preCheckId == null || chunkId == null) {
            throw new BizException("预检ID或区块ID不能为空");
        }

        return Result.success(attachService.applyChunk(preCheckId, chunkId, file));
    }
}
