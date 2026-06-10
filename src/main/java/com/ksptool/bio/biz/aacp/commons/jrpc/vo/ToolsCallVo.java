package com.ksptool.bio.biz.aacp.commons.jrpc.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * tools/call 响应：工具执行结果
 */
@Getter
@Setter
@Schema(description = "工具调用响应")
public class ToolsCallVo {

    @Schema(description = "是否错误")
    private Boolean isError;

    @Schema(description = "返回内容")
    private List<Content> content;

    @Getter
    @Setter
    @Schema(description = "内容项")
    public static class Content {

        @Schema(description = "类型 text/image/resource")
        private String type;

        @Schema(description = "文本内容")
        private String text;

        @Schema(description = "资源 URI")
        private String uri;

        @Schema(description = "MIME 类型")
        private String mimeType;
    }
}
