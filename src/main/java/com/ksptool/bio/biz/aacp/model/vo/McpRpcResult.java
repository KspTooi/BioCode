package com.ksptool.bio.biz.aacp.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * JSON-RPC 2.0 响应体
 * <p>
 * 成功时设置 {@code result}，失败时设置 {@code error}，二者互斥。
 * 嵌套类 {@link McpErrorCode} 提供 JSON-RPC 2.0 标准错误码。
 */
@Getter
@Setter
@Schema(description = "JSON-RPC 2.0 响应体")
public class McpRpcResult<T> {

    @Schema(description = "JSON-RPC版本号")
    private String jsonrpc = "2.0";

    @Schema(description = "请求ID，必须与传入的请求ID一致")
    private Integer id;

    @Schema(description = "成功的响应结果")
    private T result;

    @Schema(description = "失败的错误信息")
    private McpErrorDto error;

    /**
     * 构造成功响应
     *
     * @param id     请求ID
     * @param result 响应数据
     */
    public static <T> McpRpcResult<T> success(Integer id, T result) {
        McpRpcResult<T> r = new McpRpcResult<>();
        r.id = id;
        r.result = result;
        return r;
    }

    /**
     * 构造错误响应
     *
     * @param id      请求ID
     * @param code    错误码，建议使用 {@link McpErrorCode} 中定义的常量
     * @param message 错误描述
     */
    public static <T> McpRpcResult<T> error(Integer id, int code, String message) {
        McpRpcResult<T> r = new McpRpcResult<>();
        r.id = id;
        McpErrorDto err = new McpErrorDto();
        err.setCode(code);
        err.setMessage(message);
        r.error = err;
        return r;
    }

    /**
     * 构造携带附加数据的错误响应
     *
     * @param id      请求ID
     * @param code    错误码
     * @param message 错误描述
     * @param data    附加错误数据
     */
    public static <T> McpRpcResult<T> error(Integer id, int code, String message, Object data) {
        McpRpcResult<T> r = error(id, code, message);
        r.error.setData(data);
        return r;
    }

    /**
     * JSON-RPC 2.0 标准错误码常量
     */
    public static final class McpErrorCode {

        private McpErrorCode() {
        }

        /** 无效的 JSON 文本，服务端解析失败 */
        public static final int PARSE_ERROR = -32700;

        /** 发送的 JSON 不是有效的请求对象 */
        public static final int INVALID_REQUEST = -32600;

        /** 方法不存在或不可用 */
        public static final int METHOD_NOT_FOUND = -32601;

        /** 无效的方法参数 */
        public static final int INVALID_PARAMS = -32602;

        /** 服务端内部错误 */
        public static final int INTERNAL_ERROR = -32603;
    }

    @Getter
    @Setter
    public static class McpErrorDto {
        @Schema(description = "错误码，取值见 JSON-RPC 2.0 标准错误码")
        private Integer code;

        @Schema(description = "错误描述")
        private String message;

        @Schema(description = "附加错误数据")
        private Object data;
    }
}
