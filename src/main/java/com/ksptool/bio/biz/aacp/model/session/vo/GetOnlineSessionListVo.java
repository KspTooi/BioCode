package com.ksptool.bio.biz.aacp.model.session.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class GetOnlineSessionListVo {

    @Schema(description = "会话ID")
    private String sessionId;

    @Schema(description = "枢纽名称")
    private String hubName;

    @Schema(description = "枢纽编码")
    private String hubCode;

    @Schema(description = "连接时间")
    private LocalDateTime connectTime;

    @Schema(description = "状态 0:初始化 1:活跃")
    private Integer status;

    @Schema(description = "总请求次数")
    private Long inboundCount;
}
