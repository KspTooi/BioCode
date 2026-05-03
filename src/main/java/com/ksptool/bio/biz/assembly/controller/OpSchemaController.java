package com.ksptool.bio.biz.assembly.controller;

import com.ksptool.assembly.entity.web.CommonIdDto;
import com.ksptool.assembly.entity.web.PageResult;
import com.ksptool.assembly.entity.web.Result;
import com.ksptool.bio.biz.assembly.model.opschema.dto.*;
import com.ksptool.bio.biz.assembly.model.opschema.vo.GetOpBluePrintListVo;
import com.ksptool.bio.biz.assembly.model.opschema.vo.GetOpSchemaDetailsVo;
import com.ksptool.bio.biz.assembly.model.opschema.vo.GetOpSchemaListVo;
import com.ksptool.bio.biz.assembly.service.OpSchemaService;
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
import java.util.concurrent.locks.ReentrantLock;


@RestController
@RequestMapping("/opSchema")
@Tag(name = "ASSEMBLY-输出方案管理", description = "输出方案管理")
@Slf4j
public class OpSchemaController {

    //执行锁，防止同时执行多个输出方案，因为QBE引擎是单线程的，并发会出现线程安全问题
    private final ReentrantLock executeLock = new ReentrantLock();

    //预览锁，防止同时预览多个输出方案，因为SCM拉取和推送是单线程的，并发会出现线程安全问题
    private final ReentrantLock previewLock = new ReentrantLock();

    @Autowired
    private OpSchemaService opSchemaService;

    @PostMapping("/getOpSchemaList")
    @Operation(summary = "查询输出方案列表")
    public PageResult<GetOpSchemaListVo> getOpSchemaList(@RequestBody @Valid GetOpSchemaListDto dto) throws Exception {
        return opSchemaService.getOpSchemaList(dto);
    }

    @Operation(summary = "新增输出方案")
    @PostMapping("/addOpSchema")
    public Result<String> addOpSchema(@RequestBody @Valid AddOpSchemaDto dto) throws Exception {
        var message = opSchemaService.addOpSchema(dto);
        return Result.success(message, null);
    }

    @Operation(summary = "编辑输出方案")
    @PostMapping("/editOpSchema")
    public Result<String> editOpSchema(@RequestBody @Valid EditOpSchemaDto dto) throws Exception {
        var message = opSchemaService.editOpSchema(dto);
        return Result.success(message, null);
    }

    @Operation(summary = "查询输出方案详情")
    @PostMapping("/getOpSchemaDetails")
    public Result<GetOpSchemaDetailsVo> getOpSchemaDetails(@RequestBody @Valid CommonIdDto dto) throws Exception {
        GetOpSchemaDetailsVo details = opSchemaService.getOpSchemaDetails(dto);
        if (details == null) {
            return Result.error("无数据");
        }
        return Result.success(details);
    }

    @Operation(summary = "删除输出方案")
    @PostMapping("/removeOpSchema")
    public Result<String> removeOpSchema(@RequestBody @Valid CommonIdDto dto) throws Exception {
        opSchemaService.removeOpSchema(dto);
        return Result.success("操作成功");
    }

    @Operation(summary = "查询蓝图文件列表")
    @PostMapping("/getOpBluePrintList")
    public Result<List<GetOpBluePrintListVo>> getOpBluePrintList(@RequestBody @Valid CommonIdDto dto) throws Exception {

        if (!previewLock.tryLock()) {
            return Result.error("当前SCM服务正忙，这可能由于前一次拉取或推送操作尚未完成，请稍后再试。");
        }

        try {
            return Result.success(opSchemaService.getOpBluePrintList(dto));
        } finally {
            previewLock.unlock();
        }
    }

    @Operation(summary = "预览蓝图输出")
    @PostMapping("/previewOpBluePrint")
    public Result<String> previewOpBluePrint(@RequestBody @Valid PreviewOpBluePrintDto dto) throws Exception {

        if (!previewLock.tryLock()) {
            return Result.error("当前SCM服务正忙，这可能由于前一次拉取或推送操作尚未完成，请稍后再试。");
        }

        try {
            return Result.success(opSchemaService.previewOpBluePrint(dto.getOpSchemaId(), dto.getSha256Hex()));
        } finally {
            previewLock.unlock();
        }

    }

    @Operation(summary = "预览Qbe模型JSON")
    @PostMapping("/previewQbeModel")
    public Result<String> previewQbeModel(@RequestBody @Valid CommonIdDto dto) throws Exception {
        return Result.success(opSchemaService.previewQbeModel(dto.getId()));
    }

    @Operation(summary = "执行输出方案")
    @PostMapping("/executeOpSchema")
    public Result<String> executeOpSchema(@RequestBody @Valid ExecuteOpSchemaDto dto) throws Exception {

        if (!executeLock.tryLock()) {
            return Result.error("当前QBE引擎正忙，这可能由于前一次执行操作尚未完成，请稍后再试。");
        }

        try {
            opSchemaService.executeOpSchema(dto);
            return Result.success("操作成功");
        } finally {
            executeLock.unlock();
        }
    }

    @Operation(summary = "复制输出方案")
    @PostMapping("/copyOpSchema")
    public Result<String> copyOpSchema(@RequestBody @Valid CommonIdDto dto) throws Exception {
        opSchemaService.copyOpSchema(dto);
        return Result.success("复制成功");
    }
}
