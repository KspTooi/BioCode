package com.ksptool.bio.biz.core.model.attachpool.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class GetLatestScanRecordVo {

    @Schema(description = "存储池地址")
    private String poolPath;

    @Schema(description = "总可用字节")
    private Long poolCapacityBytes;

    @Schema(description = "附件占用字节")
    private Long poolAttachesBytes;

    @Schema(description = "已索引附件数")
    private Integer indexedCount;

    @Schema(description = "游离附件数")
    private Integer driftCount;

    @Schema(description = "扫描开始时间")
    private LocalDateTime scanStartTime;

    @Schema(description = "扫描结束时间")
    private LocalDateTime scanEndTime;

    @Schema(description = "扫描状态 0:正在扫描 1:成功")
    private Integer scanStatus;

}
