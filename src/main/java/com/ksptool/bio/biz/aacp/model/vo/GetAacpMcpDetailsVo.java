package com.ksptool.bio.biz.aacp.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class GetAacpMcpDetailsVo {

    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "服务器名称")
    private String name;

    @Schema(description = "唯一编码")
    private String code;

    @Schema(description = "通信协议 0:HTTP+SSE 1:WS")
    private Integer networkKind;

    @Schema(description = "鉴权类型 0:无 1:PSK")
    private Integer authKind;

    @Schema(description = "预共享密钥")
    private String authPsk;

    @Schema(description = "状态 0:离线 1:在线")
    private Integer status;

    @Schema(description = "关联的能力包ID列表")
    private List<Long> capabilityIds;

}
