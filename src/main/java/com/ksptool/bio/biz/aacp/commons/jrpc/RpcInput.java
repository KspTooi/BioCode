package com.ksptool.bio.biz.aacp.commons.jrpc;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * 通用 JSON-RPC 请求包装：method + 泛型 params
 *
 * @param <T> 参数类型
 */
@Getter
@Setter
@Schema(description = "JSON-RPC 通用请求")
public class RpcInput<T> {

    @Schema(description = "请求ID")
    private Integer id;

    @Schema(description = "方法名")
    private String method;

    @Schema(description = "请求参数")
    private T params;

    public static <T> RpcInput<T> of(String method, T params) {
        RpcInput<T> in = new RpcInput<>();
        in.method = method;
        in.params = params;
        return in;
    }

    public static <T> RpcInput<T> of(Integer id, String method, T params) {
        RpcInput<T> in = new RpcInput<>();
        in.id = id;
        in.method = method;
        in.params = params;
        return in;
    }
}
