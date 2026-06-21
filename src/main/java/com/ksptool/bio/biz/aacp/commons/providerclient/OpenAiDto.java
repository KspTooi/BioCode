package com.ksptool.bio.biz.aacp.commons.providerclient;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * OpenAI格式兼容请求体
 *
 * @author KspTooi
 * @since 1.7.11(K).1
 */
@Getter
@Setter
public class OpenAiDto {

    @Schema(description = "模型名称")
    private String model;

    @Schema(description = "消息列表")
    private List<?> messages;

    @Schema(description = "温度")
    private Double temperature;

    @JsonProperty("top_p")
    @Schema(description = "top_p")
    private Double top_p;

    @Schema(description = "流式输出 ")
    private Boolean stream;

    @JsonProperty("max_tokens")
    @Schema(description = "最大tokens 限制模型单次回复生成的最大 Token 数量。")
    private Integer maxTokens;

    @Schema(description = "停止序列 指定最多 4 个字符串序列。当模型生成这些特定的字符时，将立即停止生成并返回结果。")
    private List<String> stop;

    @JsonProperty("presence_penalty")
    @Schema(description = "存在惩罚（-2.0 到 2.0）。正值会惩罚已经出现过的 Token，从而增加模型谈论新话题的可能性。")
    private Integer presencePenalty;

    @JsonProperty("frequency_penalty")
    @Schema(description = "频率惩罚（-2.0 到 2.0）。正值会根据 Token 在文本中出现的频率进行惩罚，从而降低模型重复相同内容的概率。")
    private Integer frequencyPenalty;

    @Schema(description = "工具列表 指定模型可以使用的工具列表。")
    private List<?> tools;

    @JsonProperty("tool_choice")
    @Schema(description = "控制模型是否以及如何调用工具。可以是 none（不调用）、auto（自动决定）或强制调用某个特定工具。")
    private Object toolChoice;

    @JsonProperty("response_format")
    @Schema(description = "响应格式 指定模型返回的响应格式。")
    private Object responseFormat;


}
