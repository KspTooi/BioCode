package com.ksptool.bio.biz.aacp.commons.jrpc.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

/**
 * tools/list 响应：工具列表
 */
@Getter
@Setter
@Schema(description = "工具列表响应")
public class ToolsListVo {

    @Schema(description = "工具列表")
    private List<Tool> tools;

    @Getter
    @Setter
    @Schema(description = "工具定义")
    public static class Tool {

        @Schema(description = "工具名称")
        private String name;

        @Schema(description = "工具描述")
        private String description;

        @Schema(description = "入参 Schema")
        private Map<String, Object> inputSchema;
    }
}
