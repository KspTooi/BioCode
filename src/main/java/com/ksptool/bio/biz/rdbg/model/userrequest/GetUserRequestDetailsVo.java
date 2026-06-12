package com.ksptool.bio.biz.rdbg.model.userrequest;

import com.ksptool.bio.biz.rdbg.commons.model.HttpHeaderVo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class GetUserRequestDetailsVo {

    @Schema(description = "用户请求ID")
    private Long id;

    @Schema(description = "请求组ID")
    private Long groupId;

    @Schema(description = "原始请求ID")
    private Long requestId;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "用户自定义请求名称")
    private String name;

    @Schema(description = "请求方法")
    private String method;

    @Schema(description = "请求URL")
    private String url;

    @Schema(description = "请求头")
    private List<HttpHeaderVo> requestHeaders;

    @Schema(description = "请求体类型")
    private String requestBodyType;

    @Schema(description = "请求体")
    private String requestBody;

    @Schema(description = "排序")
    private Integer seq;

}

