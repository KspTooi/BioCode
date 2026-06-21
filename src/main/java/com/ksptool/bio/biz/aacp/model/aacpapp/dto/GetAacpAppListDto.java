package com.ksptool.bio.biz.aacp.model.aacpapp.dto;

import com.ksptool.assembly.entity.web.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GetAacpAppListDto extends PageQuery {

    @Schema(description = "应用名称")
    private String name;

    @Schema(description = "应用代码")
    private String code;

    @Schema(description = "状态 0:禁用 1:启用")
    private Integer status;

}
