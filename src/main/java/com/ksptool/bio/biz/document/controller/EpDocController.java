package com.ksptool.bio.biz.document.controller;


import com.ksptool.assembly.entity.exception.BizException;
import com.ksptool.assembly.entity.web.CommonIdDto;
import com.ksptool.assembly.entity.web.PageResult;
import com.ksptool.assembly.entity.web.Result;
import com.ksptool.bio.biz.document.model.epdoc.*;
import com.ksptool.bio.biz.document.service.EpDocService;
import com.ksptool.bio.commons.annotation.PrintLog;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@PrintLog
@Controller
@RestController
@RequestMapping("/epdoc")
@Tag(name = "EP-文档管理", description = "EP文档管理")
public class EpDocController {

    @Autowired
    private EpDocService epDocService;

    @PostMapping("/getEpDocList")
    public PageResult<GetEpDocListVo> getEpDocList(@RequestBody @Valid GetEpDocListDto dto) {
        return epDocService.getEpDocList(dto);
    }

    @PostMapping("/addEpDoc")
    public Result<String> addEpDoc(@RequestBody @Valid AddEpDocDto dto) throws BizException {
        epDocService.addEpDoc(dto);
        return Result.success("添加成功");
    }

    @PostMapping("/getEpDocDetails")
    public Result<GetEpDocDetailsVo> getEpDocDetails(@RequestBody @Valid CommonIdDto dto) throws BizException {
        return Result.success(epDocService.getEpDocDetails(dto.getId()));
    }

    @PostMapping("/editEpDoc")
    public Result<String> editEpDoc(@RequestBody @Valid EditEpDocDto dto) throws BizException {
        epDocService.editEpDoc(dto);
        return Result.success("编辑成功");
    }

    @PostMapping("/removeEpDoc")
    public Result<String> removeEpDoc(@RequestBody @Valid CommonIdDto dto) throws BizException {
        epDocService.deleteEpDoc(dto.getId());
        return Result.success("删除成功");
    }

    @PostMapping("/pullEpDoc")
    public Result<String> pullEpDoc(@RequestBody @Valid CommonIdDto dto) throws Exception {
        epDocService.pullEpDoc(dto.getId());
        return Result.success("拉取文档成功");
    }


}
