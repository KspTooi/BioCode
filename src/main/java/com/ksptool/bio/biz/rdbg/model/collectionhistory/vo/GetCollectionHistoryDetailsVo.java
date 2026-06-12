package com.ksptool.bio.biz.rdbg.model.collectionhistory.vo;

import com.ksptool.bio.biz.rdbg.commons.model.RelayBody;
import com.ksptool.bio.biz.rdbg.commons.model.RelayHeader;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class GetCollectionHistoryDetailsVo {

    @Schema(description = "记录ID")
    private Long id;

    @Schema(description = "集合ID")
    private Long collectionId;

    @Schema(description = "请求URL")
    private String reqUrl;

    @Schema(description = "请求URL查询参数")
    private String reqUrlParamsJson;

    @Schema(description = "请求方法 0:GET 1:POST 2:PUT 3:PATCH 4:DELETE 5:HEAD 6:OPTIONS")
    private Integer reqMethod;

    @Schema(description = "请求头JSON 类型:RelayHeader")
    private List<RelayHeader> reqHeaderJson;

    @Schema(description = "请求体JSON 类型:RelayBody")
    private RelayBody reqBodyJson;

    @Schema(description = "响应头JSON 类型:RelayHeader")
    private List<RelayHeader> retHeaderJson;

    @Schema(description = "响应体JSON 类型:RelayBody")
    private RelayBody retBodyJson;

    @Schema(description = "HTTP状态码")
    private Integer retHttpStatus;

    @Schema(description = "业务状态 0:正常 1:HTTP失败 2:业务失败 3:正在处理 4:EAS内部错误")
    private Integer bizStatus;

    @Schema(description = "请求发起时间")
    private LocalDateTime reqTime;

    @Schema(description = "响应时间")
    private LocalDateTime retTime;

    @Schema(description = "EAS内部错误消息 NULL未发生错误")
    private String errorMessage;


}

