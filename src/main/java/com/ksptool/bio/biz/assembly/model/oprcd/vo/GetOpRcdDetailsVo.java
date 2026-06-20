package com.ksptool.bio.biz.assembly.model.oprcd.vo;

import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import io.swagger.v3.oas.annotations.media.Schema;

@Getter
@Setter
public class GetOpRcdDetailsVo {

    @Schema(description="主键ID")
    private Long id;

    @Schema(description="输出方案名称")
    private String opName;

    @Schema(description="数据源名称")
    private String dsName;

    @Schema(description="数据源表名")
    private String dsTableName;

    @Schema(description="数据源连接字符串")
    private String dsUrl;

    @Schema(description="输入SCM仓库地址")
    private String scmInputUrl;

    @Schema(description="输出SCM仓库地址")
    private String scmOutputUrl;

    @Schema(description="模型名称")
    private String modelName;

    @Schema(description="模型备注")
    private String modelRemark;

    @Schema(description="业务域")
    private String bizDomain;

    @Schema(description="QBE参数")
    private String qbeParams;

    @Schema(description="开始时间")
    private LocalDateTime startTime;

    @Schema(description="结束时间")
    private LocalDateTime endTime;

    @Schema(description="耗时MS")
    private Integer durationMs;

    @Schema(description="操作人账号")
    private String creatorUsername;

}
