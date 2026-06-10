package com.ksptool.bio.biz.aacp.commons.jrpc;

import org.apache.commons.lang3.StringUtils;

/**
 * MCP 入向方法枚举（客户端 → 服务端）
 */
public enum InputMethods {

    //---- 生命周期 ----
    INITIALIZE("initialize", "初始化握手"),
    INITIALIZED_NOTIFICATION("notifications/initialized", "初始化完成通知"),

    //---- 工具 ----
    TOOLS_LIST("tools/list", "列出工具"),
    TOOLS_CALL("tools/call", "调用工具"),

    //---- 资源 ----
    RESOURCES_LIST("resources/list", "列出资源"),
    RESOURCES_READ("resources/read", "读取资源"),
    RESOURCES_TEMPLATES_LIST("resources/templates/list", "列出资源模板"),
    RESOURCES_SUBSCRIBE("resources/subscribe", "订阅资源变更"),
    RESOURCES_UNSUBSCRIBE("resources/unsubscribe", "取消订阅资源变更"),

    //---- 提示词 ----
    PROMPTS_LIST("prompts/list", "列出提示词"),
    PROMPTS_GET("prompts/get", "获取提示词"),

    //---- 补全 ----
    COMPLETION_COMPLETE("completion/complete", "自动补全"),

    //---- 实用 ----
    PING("ping", "心跳检测"),
    LOGGING_SET_LEVEL("logging/setLevel", "设置日志级别");

    private final String key;
    private final String description;

    InputMethods(String key, String description) {
        this.key = key;
        this.description = description;
    }

    public String getKey() {
        return key;
    }

    public String getDescription() {
        return description;
    }


    public static InputMethods getMethod(String method) {

        if (StringUtils.isBlank(method)) {
            return null;
        }

        for (InputMethods m : InputMethods.values()) {
            if (m.getKey().equals(method)) {
                return m;
            }
        }
        return null;
    }
}
