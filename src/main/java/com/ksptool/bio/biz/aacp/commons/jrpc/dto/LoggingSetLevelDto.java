package com.ksptool.bio.biz.aacp.commons.jrpc.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * logging/setLevel 方法参数：日志级别
 */
@Getter
@Setter
@Schema(description = "设置日志级别参数")
public class LoggingSetLevelDto {

    @Schema(description = "日志级别: debug/info/notice/warning/error/critical/alert/emergency")
    private String level;
}
