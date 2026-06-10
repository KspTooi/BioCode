package com.ksptool.bio.biz.aacp.commons;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.ksptool.bio.biz.aacp.commons.jrpc.InputMethods;
import com.ksptool.bio.biz.aacp.commons.jrpc.RpcInput;

import org.apache.commons.lang3.StringUtils;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
public class McpParser {

    private static final Gson g = new Gson();

    private final RpcInput<String> input;

    private final InputMethods method;

    /**
     * 构造函数
     *
     * @param input 输入的JSON-RPC请求
     */
    public McpParser(RpcInput<String> input) {
        this.input = input;
        this.method = InputMethods.getMethod(input.getMethod());
    }

    public static McpParser of(RpcInput<String> input) {
        return new McpParser(input);
    }

    /**
     * 从原始 JSON 体构造 RpcInput 并返回 Parser
     *
     * @param rawBody JSON-RPC 原始请求体
     * @return McpParser
     */
    public static McpParser of(String rawBody) {
        var tree = g.fromJson(rawBody, JsonObject.class);
        Integer id = tree.get("id") != null ? tree.get("id").getAsInt() : null;
        String method = tree.get("method") != null ? tree.get("method").getAsString() : null;
        String params = tree.get("params") != null ? tree.get("params").toString() : null;
        return new McpParser(RpcInput.of(id, method, params));
    }

    public <T> T as(Class<T> clazz) {
        try {

            var json = input.getParams();

            if (json == null || StringUtils.isBlank(json)) {
                return null;
            }

            return g.fromJson(json, clazz);
        } catch (Exception e) {
            log.error("Failed to parse JSON: {}", e.getMessage());
            return null;
        }
    }


}
