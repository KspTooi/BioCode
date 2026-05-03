package com.ksptool.bio.biz.document.controller;

import com.ksptool.assembly.entity.exception.BizException;
import com.ksptool.assembly.entity.web.CommonIdDto;
import com.ksptool.assembly.entity.web.Result;
import com.ksptool.bio.biz.document.model.epdocoperation.GetEpDocOperationDetailsVo;
import com.ksptool.bio.biz.document.model.epdocoperation.GetEpDocOperationTagListDto;
import com.ksptool.bio.biz.document.model.epdocoperation.GetEpDocOperationTagListVo;
import com.ksptool.bio.biz.document.service.EpDocOperationService;
import com.ksptool.bio.commons.annotation.PrintLog;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import io.swagger.v3.oas.annotations.tags.Tag;

@PrintLog
@Controller
@RestController
@RequestMapping("/epdoc")
@Tag(name = "EP-文档接口管理", description = "文档接口管理")
public class EpDocOperationController {


    @Autowired
    private EpDocOperationService epDocOperationService;


    /**
     * 获取端点文档接口标签列表
     *
     * @param dto 请求参数
     * @return 端点文档接口标签列表
     * @throws BizException 业务异常
     */
    @PostMapping("/getEpDocOperationTagList")
    public Result<List<GetEpDocOperationTagListVo>> getEpDocOperationTagList(@RequestBody @Valid GetEpDocOperationTagListDto dto) throws BizException {
        return Result.success(epDocOperationService.getEpDocOperationTagList(dto));
    }


    /**
     * 获取端点文档接口详情
     *
     * @param dto 请求参数
     * @return 端点文档接口详情
     * @throws BizException 业务异常
     */
    @PostMapping("/getEpDocOperationDetails")
    public Result<GetEpDocOperationDetailsVo> getEpDocOperationDetails(@RequestBody @Valid CommonIdDto dto) throws BizException {
        return Result.success(epDocOperationService.getEpDocOperationDetails(dto.getId()));
    }


}
