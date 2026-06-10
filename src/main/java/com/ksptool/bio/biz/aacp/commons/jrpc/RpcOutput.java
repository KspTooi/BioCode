package com.ksptool.bio.biz.aacp.commons.jrpc;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * 通用 JSON-RPC 响应包装：result 成功 / error 失败
 *
 * @param <T> 结果类型
 */
@Getter
@Setter
@Schema(description = "JSON-RPC 通用响应")
public class RpcOutput<T> {

    @Schema(description = "JSON-RPC版本号")
    private String jsonrpc = "2.0";

    @Schema(description = "请求ID，与请求中的ID一致")
    private Integer id;

    @Schema(description = "成功的响应结果")
    private T result;

    @Schema(description = "失败的错误信息")
    private RpcError error;

    public static <T> RpcOutput<T> success(Integer id, T result) {
        RpcOutput<T> out = new RpcOutput<>();
        out.id = id;
        out.result = result;
        return out;
    }

    public static <T> RpcOutput<T> error(Integer id, int code, String message) {
        RpcOutput<T> out = new RpcOutput<>();
        out.id = id;
        RpcError err = new RpcError();
        err.code = code;
        err.message = message;
        out.error = err;
        return out;
    }

    @Getter
    @Setter
    @Schema(description = "JSON-RPC 错误信息")
    public static class RpcError {

        @Schema(description = "错误码")
        private int code;

        @Schema(description = "错误描述")
        private String message;
    }
}
