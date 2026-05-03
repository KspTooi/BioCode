package com.ksptool.bio.biz.rdbg.controller;

import com.ksptool.bio.biz.rdbg.model.collectionhistory.dto.AddCollectionHistoryDto;
import com.ksptool.bio.biz.rdbg.model.collectionhistory.dto.EditCollectionHistoryDto;
import com.ksptool.bio.biz.rdbg.model.collectionhistory.dto.GetCollectionHistoryListDto;
import com.ksptool.bio.biz.rdbg.model.collectionhistory.vo.GetCollectionHistoryDetailsVo;
import com.ksptool.bio.biz.rdbg.model.collectionhistory.vo.GetCollectionHistoryListVo;
import com.ksptool.bio.biz.rdbg.service.CollectionHistoryService;
import com.ksptool.assembly.entity.web.CommonIdDto;
import com.ksptool.assembly.entity.web.PageResult;
import com.ksptool.assembly.entity.web.Result;
import com.ksptool.bio.commons.annotation.PrintLog;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@PrintLog
@RestController
@RequestMapping("/collectionHistory")
@Tag(name = "RDBG-请求集合历史记录", description = "请求集合历史记录管理")
@Slf4j
public class CollectionHistoryController {

    @Autowired
    private CollectionHistoryService collectionHistoryService;

    @PostMapping("/getCollectionHistoryList")
    @Operation(summary = "查询请求集合历史记录列表")
    public PageResult<GetCollectionHistoryListVo> getCollectionHistoryList(@RequestBody @Valid GetCollectionHistoryListDto dto) throws Exception {
        return collectionHistoryService.getCollectionHistoryList(dto);
    }

    @Operation(summary = "添加请求集合历史记录")
    @PostMapping("/addCollectionHistory")
    public Result<String> addCollectionHistory(@RequestBody @Valid AddCollectionHistoryDto dto) throws Exception {
        collectionHistoryService.addCollectionHistory(dto);
        return Result.success("新增成功");
    }

    @Operation(summary = "编辑请求集合历史记录")
    @PostMapping("/editCollectionHistory")
    public Result<String> editCollectionHistory(@RequestBody @Valid EditCollectionHistoryDto dto) throws Exception {
        collectionHistoryService.editCollectionHistory(dto);
        return Result.success("修改成功");
    }

    @Operation(summary = "查询请求集合历史记录详情")
    @PostMapping("/getCollectionHistoryDetails")
    public Result<GetCollectionHistoryDetailsVo> getCollectionHistoryDetails(@RequestBody @Valid CommonIdDto dto) throws Exception {
        GetCollectionHistoryDetailsVo details = collectionHistoryService.getCollectionHistoryDetails(dto);
        if (details == null) {
            return Result.error("无数据");
        }
        return Result.success(details);
    }

    @Operation(summary = "删除请求集合历史记录")
    @PostMapping("/removeCollectionHistory")
    public Result<String> removeCollectionHistory(@RequestBody @Valid CommonIdDto dto) throws Exception {
        collectionHistoryService.removeCollectionHistory(dto);
        return Result.success("操作成功");
    }

}
