package com.ksptool.bio.biz.audit.model.auditerrorrcd.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class GetAuditErrorRcdListVo {

    @Schema(description = "错误ID")
    private Long id;

    @Schema(description = "错误代码")
    private String errorCode;

    @Schema(description = "请求地址")
    private String requestUri;

    @Schema(description = "操作人用户名")
    private String userName;

    @Schema(description = "异常类型")
    private String errorType;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

}

