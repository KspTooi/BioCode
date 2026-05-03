package com.ksptool.bio.biz.rdbg.controller;

import com.ksptool.assembly.entity.exception.AuthException;
import com.ksptool.assembly.entity.web.CommonIdDto;
import com.ksptool.assembly.entity.web.Result;
import com.ksptool.bio.biz.rdbg.model.collection.dto.AddCollectionDto;
import com.ksptool.bio.biz.rdbg.model.collection.dto.EditCollectionDto;
import com.ksptool.bio.biz.rdbg.model.collection.dto.MoveCollectionDto;
import com.ksptool.bio.biz.rdbg.model.collection.vo.GetCollectionDetailsVo;
import com.ksptool.bio.biz.rdbg.model.collection.vo.GetCollectionTreeVo;
import com.ksptool.bio.biz.rdbg.service.CollectionService;
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

import java.util.List;
import java.util.Map;

import static com.ksptool.bio.biz.auth.service.SessionService.session;

@PrintLog
@RestController
@RequestMapping("/collection")
@Tag(name = "RDBG-请求集合", description = "请求集合管理")
@Slf4j
public class CollectionController {

    @Autowired
    private CollectionService collectionService;


    @PostMapping("/getCollectionTree")
    @Operation(summary = "查询请求集合列表树")
    public Result<List<GetCollectionTreeVo>> getCollectionTree() throws Exception {

        if (session().getRootId() == null) {
            return Result.error(101, "该操作需要用户加入团队后才能执行");
        }

        return Result.success(collectionService.getCollectionTree());
    }

    @Operation(summary = "新增请求集合")
    @PostMapping("/addCollection")
    public Result<String> addCollection(@RequestBody @Valid AddCollectionDto dto) throws Exception {

        if (session().getRootId() == null) {
            return Result.error(101, "该操作需要用户加入团队后才能执行");
        }

        collectionService.addCollection(dto);
        return Result.success("新增成功");
    }

    @Operation(summary = "移动请求集合")
    @PostMapping("/moveCollection")
    public Result<String> moveCollection(@RequestBody @Valid MoveCollectionDto dto) throws Exception {

        if (session().getRootId() == null) {
            return Result.error(101, "该操作需要用户加入团队后才能执行");
        }

        collectionService.moveCollection(dto);
        return Result.success("移动操作成功");
    }

    @Operation(summary = "复制请求集合")
    @PostMapping("/copyCollection")
    public Result<String> copyCollection(@RequestBody @Valid CommonIdDto dto) throws Exception {

        if (session().getRootId() == null) {
            return Result.error(101, "该操作需要用户加入团队后才能执行");
        }

        collectionService.copyCollection(dto);
        return Result.success("复制操作成功");
    }

    @Operation(summary = "编辑请求集合")
    @PostMapping("/editCollection")
    public Result<String> editCollection(@RequestBody @Valid EditCollectionDto dto) throws Exception {

        if (session().getRootId() == null) {
            return Result.error(101, "该操作需要用户加入团队后才能执行");
        }

        collectionService.editCollection(dto);
        return Result.success("修改成功");
    }

    @Operation(summary = "查询请求集合详情")
    @PostMapping("/getCollectionDetails")
    public Result<GetCollectionDetailsVo> getCollectionDetails(@RequestBody @Valid CommonIdDto dto) throws Exception {

        if (session().getRootId() == null) {
            return Result.error(101, "该操作需要用户加入团队后才能执行");
        }

        GetCollectionDetailsVo details = collectionService.getCollectionDetails(dto);

        if (details == null) {
            return Result.error("无数据");
        }

        return Result.success(details);
    }

    @Operation(summary = "删除请求集合")
    @PostMapping("/removeCollection")
    public Result<String> removeCollection(@RequestBody @Valid CommonIdDto dto) throws Exception {

        if (session().getRootId() == null) {
            return Result.error(101, "该操作需要用户加入团队后才能执行");
        }

        collectionService.removeCollection(dto);
        return Result.success("操作成功");
    }


    @Operation(summary = "发送请求")
    @PostMapping("/sendRequest")
    public Result<Map<String, Object>> sendRequest(@RequestBody @Valid CommonIdDto dto) throws AuthException {

        if (session().getRootId() == null) {
            return Result.error(101, "该操作需要用户加入团队后才能执行");
        }

        //return Result.success(hRelay.sendRequest(dto));
        return null;
    }


}
