package com.ksptool.bio.biz.aacp.commons.jrpc.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * resources/unsubscribe 方法参数：取消订阅的资源 URI
 */
@Getter
@Setter
@Schema(description = "取消订阅资源参数")
public class ResourcesUnsubscribeDto {

    @Schema(description = "资源URI")
    private String uri;
}
