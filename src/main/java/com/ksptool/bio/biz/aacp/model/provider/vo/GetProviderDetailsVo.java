package com.ksptool.bio.biz.aacp.model.provider.vo;

import lombok.Getter;
import lombok.Setter;
import io.swagger.v3.oas.annotations.media.Schema;

@Getter
@Setter
public class GetProviderDetailsVo {

    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "供应商名称")
    private String name;

    @Schema(description = "供应商代码")
    private String code;

    @Schema(description = "接口类型 0:OpenAi 1:Anthropic")
    private Integer apiKind;

    @Schema(description = "接口地址")
    private String apiHost;

    @Schema(description = "接口端点")
    private String apiUrl;

    @Schema(description = "代理类型 0:无 1:HTTP 2:SOCKS5")
    private Integer proxyKind;

    @Schema(description = "代理地址")
    private String proxyUrl;

    @Schema(description = "状态 0:禁用 1:启用")
    private Integer status;
}
