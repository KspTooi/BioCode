package com.ksptool.bio.biz.document.controller;

import com.ksptool.assembly.entity.web.PageResult;
import com.ksptool.bio.biz.document.model.epdocsynclog.GetEpDocSyncLogListDto;
import com.ksptool.bio.biz.document.model.epdocsynclog.GetEpDocSyncLogListVo;
import com.ksptool.bio.biz.document.service.EpDocSyncLogService;
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
@RequestMapping("/epdocSyncLog")
@Tag(name = "EP-文档拉取记录管理", description = "文档拉取记录管理")
public class EpDocSyncLogController {

    @Autowired
    private EpDocSyncLogService epDocSyncLogService;

    /**
     * 获取端点文档拉取记录列表
     *
     * @param dto 查询条件
     * @return 端点文档拉取记录列表
     */
    @PostMapping("/getEpSyncLogList")
    public PageResult<GetEpDocSyncLogListVo> getEpSyncLogList(@RequestBody @Valid GetEpDocSyncLogListDto dto) {
        return epDocSyncLogService.getEpSyncLogList(dto);
    }

}
