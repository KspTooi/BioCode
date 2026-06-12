package com.ksptool.bio.biz.auth.model.basicpat.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class GetBasicPatListVo {

    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "PAT名称")
    private String name;

    @Schema(description = "部分明文")
    private String patPt;

    @Schema(description = "过期时间")
    private LocalDateTime expire;

    @Schema(description = "状态: 0:禁用 1:启用")
    private Integer status;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

}
