package com.ksptool.bio.biz.aacp.model.model.dto;

import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import io.swagger.v3.oas.annotations.media.Schema;

@Getter
@Setter
public class AddModelDto {

    @Schema(description="模型变体名称")
    private String name;

    @Schema(description="模型标识")
    private String code;

    @Schema(description="类型 0:文本 1:图形 2:音频 3:多模态")
    private Integer kind;

    @Schema(description="最大上下文长度")
    private Integer maxContext;

    @Schema(description="最大输出词元")
    private Integer maxOutputToken;

    @Schema(description="推理 0:不支持 1:支持")
    private Integer apiReasoning;

    @Schema(description="推理强度 0:关 1:低 2:中 3:高 4:极高")
    private Integer apiReasoningEffort;

    @Schema(description="附加参数")
    private String apiAppendParam;

    @Schema(description="附加请求头")
    private String apiAppendHeaders;

    @Schema(description="输入单价")
    private String fincInput;

    @Schema(description="输入单价(缓存)")
    private String fincInputCached;

    @Schema(description="输出单价")
    private String fincOutput;

    @Schema(description="测试首字响应时间 MS")
    private Integer testTtfb;

    @Schema(description="测试响应速率 T/S")
    private Integer testRate;

    @Schema(description="最后测试时间")
    private LocalDateTime testTime;

    @Schema(description="备注")
    private String remark;

    @Schema(description="排序")
    private Integer seq;

    @Schema(description="状态 0:禁用 1:启用")
    private Integer status;

}
