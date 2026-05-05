package com.ksptool.bio.biz.core.model.root.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class GetCoreRootDetailsVo {

    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "租户名称")
    private String name;

    @Schema(description = "到期时间")
    private LocalDateTime expireTime;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "状态 0:正常 1:禁用")
    private Integer status;

    @Schema(description = "内置租户 0:否 1:是")
    private Integer isSystem;

}
