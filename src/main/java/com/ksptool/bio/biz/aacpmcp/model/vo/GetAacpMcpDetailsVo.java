package com.ksptool.bio.biz.aacpmcp.model.vo;

import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import io.swagger.v3.oas.annotations.media.Schema;

@Getter
@Setter
public class GetAacpMcpDetailsVo {

    @Schema(description="主键ID")
    private Long id;

    @Schema(description="服务器名称")
    private String name;

    @Schema(description="唯一编码")
    private String code;

    @Schema(description="通信协议 0:HTTP+SSE 1:WS")
    private Integer networkKind;

    @Schema(description="主机")
    private String host;

    @Schema(description="端口")
    private Integer port;

    @Schema(description="鉴权类型 0:无 1:PSK")
    private Integer authKind;

    @Schema(description="预共享密钥")
    private String authPsk;

    @Schema(description="状态 0:离线 1:在线")
    private Integer status;

}
