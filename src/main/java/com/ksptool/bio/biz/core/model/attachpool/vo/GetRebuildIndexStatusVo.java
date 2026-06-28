package com.ksptool.bio.biz.core.model.attachpool.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class GetRebuildIndexStatusVo {

    @Schema(description = "是否执行中")
    private Boolean running;

    @Schema(description = "游离文件总数")
    private Integer total;

    @Schema(description = "已处理数")
    private Integer processed;

    @Schema(description = "新建rebuild_index数")
    private Integer imported;

    @Schema(description = "修复索引数")
    private Integer repaired;

    @Schema(description = "删除重复游离数")
    private Integer deleted;

    @Schema(description = "失败数")
    private Integer failed;

    @Schema(description = "任务摘要")
    private String message;

    @Schema(description = "开始时间")
    private LocalDateTime startTime;

    @Schema(description = "结束时间")
    private LocalDateTime endTime;

}
