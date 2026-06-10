package com.ksptool.bio.biz.aacp.model.mcp.dto;

import com.ksptool.assembly.entity.web.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GetAacpMcpListDto extends PageQuery {

    @Schema(description = "服务器名称")
    private String name;

    @Schema(description = "唯一编码")
    private String code;

    @Schema(description = "状态 0:离线 1:在线")
    private Integer status;

}
