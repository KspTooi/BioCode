package com.ksptool.bio.biz.aacp.commons.jrpc.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * resources/read 方法参数：资源 URI
 */
@Getter
@Setter
@Schema(description = "读取资源参数")
public class ResourcesReadDto {

    @Schema(description = "资源URI")
    private String uri;
}
