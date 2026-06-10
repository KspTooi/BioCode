package com.ksptool.bio.biz.aacp.commons.jrpc.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * prompts/get 响应：获取提示词
 */
@Getter
@Setter
@Schema(description = "获取提示词响应")
public class PromptsGetVo {

    @Schema(description = "提示词描述")
    private String description;

    @Schema(description = "消息列表")
    private List<Message> messages;

    @Getter
    @Setter
    @Schema(description = "提示词消息")
    public static class Message {

        @Schema(description = "角色 user/assistant")
        private String role;

        @Schema(description = "多段内容")
        private Content content;

        @Getter
        @Setter
        @Schema(description = "消息内容")
        public static class Content {

            @Schema(description = "类型 text/image/resource")
            private String type;

            @Schema(description = "文本")
            private String text;
        }
    }
}
