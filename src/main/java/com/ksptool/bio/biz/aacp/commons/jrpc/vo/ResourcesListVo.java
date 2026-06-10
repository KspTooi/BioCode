package com.ksptool.bio.biz.aacp.commons.jrpc.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * resources/list 响应：资源列表
 */
@Getter
@Setter
@Schema(description = "资源列表响应")
public class ResourcesListVo {

    @Schema(description = "资源列表")
    private List<Resource> resources;

    @Getter
    @Setter
    @Schema(description = "资源定义")
    public static class Resource {

        @Schema(description = "资源 URI")
        private String uri;

        @Schema(description = "资源名称")
        private String name;

        @Schema(description = "资源描述")
        private String description;

        @Schema(description = "MIME 类型")
        private String mimeType;
    }
}
