package com.ksptool.bio.biz.aacp.commons.jrpc.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * ping 响应
 */
@Getter
@Setter
@Schema(description = "心跳响应")
public class PingVo {

    @Schema(description = "是否在线")
    private Boolean pong = true;
}
