package com.ksptool.bio.biz.aacp.commons.jrpc.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

/**
 * prompts/list 响应：提示词列表
 */
@Getter
@Setter
@Schema(description = "提示词列表响应")
public class PromptsListVo {

    @Schema(description = "提示词列表")
    private List<Prompt> prompts;

    @Getter
    @Setter
    @Schema(description = "提示词定义")
    public static class Prompt {

        @Schema(description = "提示词名称")
        private String name;

        @Schema(description = "提示词描述")
        private String description;

        @Schema(description = "参数定义")
        private List<Argument> arguments;

        @Getter
        @Setter
        @Schema(description = "参数定义")
        public static class Argument {

            @Schema(description = "参数名")
            private String name;

            @Schema(description = "参数描述")
            private String description;

            @Schema(description = "是否必填")
            private Boolean required;
        }
    }
}
