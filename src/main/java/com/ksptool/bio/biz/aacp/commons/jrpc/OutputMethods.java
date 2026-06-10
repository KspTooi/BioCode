package com.ksptool.bio.biz.aacp.commons.jrpc;

/**
 * MCP 出向方法枚举（服务端 → 客户端）
 */
public enum OutputMethods {

    //---- 工具通知 ----
    TOOLS_LIST_CHANGED("notifications/tools/list_changed", "工具列表变更通知"),

    //---- 资源通知 ----
    RESOURCES_LIST_CHANGED("notifications/resources/list_changed", "资源列表变更通知"),
    RESOURCES_UPDATED("notifications/resources/updated", "资源内容变更通知"),

    //---- 提示词通知 ----
    PROMPTS_LIST_CHANGED("notifications/prompts/list_changed", "提示词列表变更通知"),

    //---- 日志通知 ----
    LOGGING_MESSAGE("notifications/message", "日志消息通知"),

    //---- 服务端主动请求 ----
    SAMPLING_CREATE_MESSAGE("sampling/createMessage", "请求客户端LLM采样"),
    ROOTS_LIST("roots/list", "请求客户端根URI列表");

    private final String key;
    private final String description;

    OutputMethods(String key, String description) {
        this.key = key;
        this.description = description;
    }

    public String getKey() {
        return key;
    }

    public String getDescription() {
        return description;
    }
}
