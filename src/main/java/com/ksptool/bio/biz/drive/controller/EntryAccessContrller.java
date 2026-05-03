package com.ksptool.bio.biz.drive.controller;

import com.ksptool.assembly.entity.exception.BizException;
import com.ksptool.assembly.entity.web.CommonIdDto;
import com.ksptool.assembly.entity.web.Result;
import com.ksptool.bio.biz.drive.model.driveentry.vo.GetEntrySignVo;
import com.ksptool.bio.biz.drive.service.EntryAccessService;
import com.ksptool.bio.biz.drive.utils.DriveEntrySignUtils;
import com.ksptool.bio.commons.annotation.PrintLog;
import com.ksptool.bio.commons.config.DriveConfig;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.tika.Tika;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

@PrintLog
@RestController
@RequestMapping("/drive/object/access")
@Tag(name = "DRIVE-云盘对象访问接口", description = "云盘对象访问接口")
@Slf4j
public class EntryAccessContrller {

    private final Tika tika = new Tika();

    @Autowired
    private EntryAccessService entryAccessService;

    @Autowired
    private DriveConfig driveConfig;

    @PreAuthorize("@auth.hasCode('drive:entry:access:sign')")
    @Operation(summary = "获取条目对象签名", description = "允许用户在云盘内获取文件签名")
    @PostMapping("/getEntrySign")
    public Result<GetEntrySignVo> getEntrySign(@RequestBody @Valid CommonIdDto dto) throws Exception {

        if (!dto.isValid()) {
            return Result.error("条目ID不能为空");
        }

        var ret = new GetEntrySignVo();
        ret.setParams(entryAccessService.getEntrySign(dto.toIds()));
        ret.setIsBatch(dto.toIds().size() > 1 ? 1 : 0);
        return Result.success(ret);
    }

    @PreAuthorize("@auth.hasCode('drive:entry:access:download')")
    @Operation(summary = "下载条目", description = "允许用户在云盘内下载文件")
    @GetMapping("/downloadEntry")
    @PostMapping("/downloadEntry")
    public ResponseEntity<Resource> downloadEntry(@RequestParam("sign") String sign
            , @RequestParam(value = "preview", required = false, defaultValue = "0") Integer preview, HttpServletResponse hsrp) throws Exception {

        if (StringUtils.isBlank(sign)) {
            throw new BizException("签名参数不能为空");
        }

        //校验preview参数 0:下载 1:预览
        if (preview == null || (preview != 0 && preview != 1)) {
            throw new BizException("preview参数不合法.");
        }

        //验证签名
        var signVo = DriveEntrySignUtils.parserSignWithParams(sign, driveConfig.getSignSecretKey());

        //判断签名是否过期
        if (System.currentTimeMillis() > (signVo.getT() + driveConfig.getTtl() * 1000)) {
            throw new BizException("签名已过期! 请重新生成签名并尝试下载.");
        }

        //预览模式
        if (preview == 1) {

            //不支持多签预览
            if (signVo.getIsBatch() == 1) {
                throw new BizException("签名的文件数量超过1个,无法预览.");
            }

            var resource = entryAccessService.downloadEntry(signVo);

            //探测Mine类型
            var mimeType = tika.detect(resource.getFile());
            var filename = URLEncoder.encode(signVo.getEName(), StandardCharsets.UTF_8).replaceAll("\\+", "%20");
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename*=UTF-8''" + filename)
                    .contentType(MediaType.parseMediaType(mimeType))
                    .body(resource);
        }


        //单签直接下载单个文件
        if (signVo.getIsBatch() == 0) {
            var resource = entryAccessService.downloadEntry(signVo);
            var filename = URLEncoder.encode(signVo.getEName(), StandardCharsets.UTF_8).replaceAll("\\+", "%20");

            //判断FileName是否包含扩展名
            if (!filename.contains(".")) {

                var attachName = resource.getFilename();

                //如果附件名称包含扩展名，则使用附件的扩展名
                if (attachName != null && attachName.contains(".")) {
                    //获取附件中最后一个.后的扩展名
                    var attachSuffix = attachName.substring(attachName.lastIndexOf("."));
                    filename = URLEncoder.encode(signVo.getEName() + attachSuffix, StandardCharsets.UTF_8).replaceAll("\\+", "%20");
                }
            }

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + filename)
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(resource);
        }

        var name = "多个文件(" + Arrays.stream(signVo.getEids().split(",")).count() + ").zip";
        var encodedName = URLEncoder.encode(name, StandardCharsets.UTF_8).replaceAll("\\+", "%20");
        hsrp.setContentType("application/zip");
        hsrp.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + encodedName);
        hsrp.setCharacterEncoding("utf-8");
        //多签打包下载
        entryAccessService.downloadBatchEntry(signVo, hsrp.getOutputStream());
        return null;
    }


}
