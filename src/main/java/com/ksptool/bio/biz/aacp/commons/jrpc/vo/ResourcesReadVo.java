package com.ksptool.bio.biz.aacp.commons.jrpc.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * resources/read 响应：资源内容
 */
@Getter
@Setter
@Schema(description = "资源读取响应")
public class ResourcesReadVo {

    @Schema(description = "资源内容列表")
    private List<Content> contents;

    @Getter
    @Setter
    @Schema(description = "资源内容")
    public static class Content {

        @Schema(description = "资源 URI")
        private String uri;

        @Schema(description = "MIME 类型")
        private String mimeType;

        @Schema(description = "文本内容")
        private String text;

        @Schema(description = "二进制(BASE64)")
        private String blob;
    }
}
