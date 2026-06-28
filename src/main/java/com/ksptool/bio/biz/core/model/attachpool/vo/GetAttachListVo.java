package com.ksptool.bio.biz.core.model.attachpool.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class GetAttachListVo {

    @Schema(description = "文件路径")
    private String path;

    @Schema(description = "文件摘要")
    private String sha256;

    @Schema(description = "文件总大小")
    private Long totalSize;

    @Schema(description = "已接收大小")
    private Long receiveSize;

    @Schema(description = "状态 0:预检文件 1:区块不完整 2:校验中 3:有效")
    private Integer status;

    @Schema(description = "校验时间")
    private LocalDateTime verifyTime;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

}
