package com.ksptool.bio.commons.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class HttpRelaySchemaConfig {

    //HTTP连接超时时间(ms) 这是客户端尝试与服务器建立连接的时间
    private Integer connectTimeout;

    //HTTP读取超时时间(ms) 连接已建立，客户端已发送请求。这是客户端等待服务器返回响应数据的最长时间。
    private Integer readTimeout;

    //HTTP写入超时时间(ms) 指客户端将请求数据写入 Socket 缓冲区的最长时间
    private Integer writeTimeout;

    private HttpRelaySchemaConfig() {
        this.connectTimeout = 3000;
        this.readTimeout = 3000;
        this.writeTimeout = 3000;
    }

    public static HttpRelaySchemaConfig ofDefault() {
        return new HttpRelaySchemaConfig();
    }

}
