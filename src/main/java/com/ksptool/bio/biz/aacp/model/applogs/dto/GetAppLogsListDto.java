package com.ksptool.bio.biz.aacp.model.applogs.dto;

import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import com.ksptool.assembly.entity.web.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GetAppLogsListDto extends PageQuery {

    @Schema(description="应用ID")
    private Long appId;

    @Schema(description="供应商ID")
    private Long providerId;

    @Schema(description="模型变体ID")
    private Long modelId;

    @Schema(description="输入词元")
    private Integer inputToken;

    @Schema(description="输出词元")
    private Integer outputToken;

    @Schema(description="消耗金额")
    private String cost;

    @Schema(description="发起时间")
    private LocalDateTime startTime;

    @Schema(description="结束时间")
    private LocalDateTime endTime;

    @Schema(description="总耗时MS")
    private Integer durationMs;

    @Schema(description="首字响应时间")
    private Integer ttfbMs;

    @Schema(description="HTTP状态码")
    private String statusCode;

    @Schema(description="客户端IP")
    private String clientIp;

}
