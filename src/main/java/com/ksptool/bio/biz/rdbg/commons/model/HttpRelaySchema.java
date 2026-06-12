package com.ksptool.bio.commons.model;

import io.micrometer.common.util.StringUtils;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
public class HttpRelaySchema {

    //hop-by-hop请求头
    @Getter
    private final Set<String> HOP_BY_HOP_HEADERS;

    //请求URL
    private String url;

    //URL中的参数
    @Setter
    private List<RelayParam> queryParams;

    //请求方法 0:GET 1:POST 2:PUT 3:PATCH 4:DELETE 5:HEAD 6:OPTIONS
    private Integer method;

    //请求头
    @Setter
    private Set<RelayHeader> headers;

    //请求体
    @Setter
    private RelayBody body;

    //配置
    @Setter
    private HttpRelaySchemaConfig config;

    private HttpRelaySchema() {
        this.HOP_BY_HOP_HEADERS = HopByHopHeaders.ofDefault();
        this.url = null;
        this.queryParams = new ArrayList<>();
        this.method = 0;
        this.headers = new HashSet<>();
        this.body = RelayBody.ofDefault();
        this.config = HttpRelaySchemaConfig.ofDefault();
    }

    public static HttpRelaySchema ofDefault() {
        return new HttpRelaySchema();
    }

    public static HttpRelaySchema of(String url) {
        HttpRelaySchema hrs = new HttpRelaySchema();
        hrs.setUrlWithQueryParams(url);
        return hrs;
    }

    public static HttpRelaySchema of(String url, Integer method) {
        HttpRelaySchema hrs = new HttpRelaySchema();
        hrs.setUrlWithQueryParams(url);
        hrs.setMethod(method);
        return hrs;
    }


    /**
     * 移除hop-by-hop请求头
     *
     * @param header 请求头
     */
    public void removeHopByHopHeaders(String header) {
        HOP_BY_HOP_HEADERS.remove(header);
    }

    /**
     * 添加hop-by-hop请求头
     *
     * @param header 请求头
     */
    public void addHopByHopHeaders(String header) {
        HOP_BY_HOP_HEADERS.add(header);
    }

    /**
     * 清空hop-by-hop请求头
     */
    public void clearHopByHopHeaders() {
        HOP_BY_HOP_HEADERS.clear();
    }

    /**
     * 设置请求方法
     *
     * @param method 请求方法 0:GET 1:POST 2:PUT 3:PATCH 4:DELETE 5:HEAD 6:OPTIONS
     */
    public void setMethod(Integer method) {
        if (method == null || method < 0 || method > 6) {
            throw new IllegalArgumentException("请求方法不合法,method: " + method);
        }
        this.method = method;
    }

    /**
     * 设置URL并解析查询参数
     *
     * @param url URL
     */
    public void setUrlWithQueryParams(String url) {
        if (StringUtils.isBlank(url)) {
            throw new IllegalArgumentException("URL不能为空");
        }
        //解析URL
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(url);

        //从URL中解析出查询参数
        this.setQueryParams(RelayParam.ofMultiValueMap(builder.build().getQueryParams()));

        //设置不带查询参数的URL
        this.url = builder.build().toUriString();
    }


    public String getMethodString() {
        if (this.method == null || this.method < 0 || this.method > 6) {
            throw new IllegalArgumentException("请求方法不合法,method: " + this.method);
        }

        if (this.method == 0) {
            return "GET";
        }
        if (this.method == 1) {
            return "POST";
        }
        if (this.method == 2) {
            return "PUT";
        }
        if (this.method == 3) {
            return "PATCH";
        }
        if (this.method == 4) {
            return "DELETE";
        }
        if (this.method == 5) {
            return "HEAD";
        }
        if (this.method == 6) {
            return "OPTIONS";
        }

        throw new IllegalArgumentException("请求方法不合法,method: " + this.method);
    }

}
