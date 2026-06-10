package com.ksptool.bio.biz.aacp.commons.jrpc.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * resources/subscribe 方法参数：订阅的资源 URI
 */
@Getter
@Setter
@Schema(description = "订阅资源参数")
public class ResourcesSubscribeDto {

    @Schema(description = "资源URI")
    private String uri;
}
