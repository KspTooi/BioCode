package com.ksptool.bio.biz.document.model.epdocoperation;

import com.ksptool.bio.biz.rdbg.commons.model.BodySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class GetEpDocOperationDetailsVo {

    @Schema(description = "接口ID")
    private Long id;

    @Schema(description = "接口路径")
    private String path;

    @Schema(description = "请求方法")
    private String method;

    @Schema(description = "接口摘要")
    private String summary;

    @Schema(description = "接口描述")
    private String description;

    @Schema(description = "请求体")
    private BodySchema reqBody;

    @Schema(description = "响应体")
    private BodySchema resBody;

    @Schema(description = "唯一操作ID")
    private String operationId;


}
