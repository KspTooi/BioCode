package com.ksptool.bio.biz.core.model.root.dto;

import com.ksptool.assembly.entity.web.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class GetCoreRootListDto extends PageQuery {

    @Schema(description = "租户名称")
    private String name;

    @Schema(description = "到期时间范围-开始")
    private LocalDateTime expireTimeRangeStart;

    @Schema(description = "到期时间范围-结束")
    private LocalDateTime expireTimeRangeEnd;

    @Schema(description = "状态 0:正常 1:禁用")
    private Integer status;

}
