package com.ksptool.bio.biz.rdbg.commons.model;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Type;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpRequest;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
@Setter
public class RequestSchema {

    //hop-by-hop请求头
    private static final Set<String> HOP_BY_HOP_HEADERS = new HashSet<>();

    static {
        HOP_BY_HOP_HEADERS.add("connection");
        HOP_BY_HOP_HEADERS.add("keep-alive");
        HOP_BY_HOP_HEADERS.add("proxy-authenticate");
        HOP_BY_HOP_HEADERS.add("proxy-authorization");
        HOP_BY_HOP_HEADERS.add("te");
        HOP_BY_HOP_HEADERS.add("trailer");
        HOP_BY_HOP_HEADERS.add("transfer-encoding");
        HOP_BY_HOP_HEADERS.add("upgrade");
        HOP_BY_HOP_HEADERS.add("host");
        HOP_BY_HOP_HEADERS.add("content-length");
    }

    private final Gson gson = new Gson();
    private String url;

    private String method;

    private List<HttpHeaderVo> headers = new ArrayList<>();

    private byte[] body;

    private String contentType;

    /**
     * 替换URL中的主机名、协议和端口号 例如:http://www.baidu.com/index.html 替换为 http://127.0.0.1:8080/index.html
     *
     * @param url       原始URL
     * @param targetUrl 目标URL
     * @return 替换后的URL
     */
    private static String replaceHostAndScheme(String url, String targetUrl) {
        try {
            URI uri = new URI(url);
            URI targetUri = new URI(targetUrl);
            return new URI(targetUri.getScheme(), targetUri.getUserInfo(), targetUri.getHost(), targetUri.getPort(), uri.getPath(), uri.getQuery(), uri.getFragment()).toString();
        } catch (URISyntaxException e) {
            return url;
        }
    }

    /**
     * 将JSON格式的请求头处理为VO
     *
     * @param json JSON格式的请求头
     */
    public void parserHeaderFromJson(String json) {

        if (StringUtils.isBlank(json)) {
            return;
        }

        //将JSON格式的请求头处理为VO
        List<HttpHeaderVo> headersList = new ArrayList<>();
        if (StringUtils.isNotBlank(json)) {
            Type listType = new TypeToken<List<HttpHeaderVo>>() {
            }.getType();
            headersList = gson.fromJson(json, listType);
        }

        this.headers.clear();

        //将VO加入到headers
        for (var it : headersList) {
            if (HOP_BY_HOP_HEADERS.contains(it.getK().toLowerCase())) {
                continue;
            }
            this.headers.add(it);
        }

    }

    /**
     * 设置请求头
     *
     * @param key   请求头键
     * @param value 请求头值
     */
    public void setHeader(String key, String value) {
        //查找是否存在
        for (var it : headers) {
            //如果存在则更新
            if (it.getK().equalsIgnoreCase(key)) {
                it.setV(value);
                return;
            }
        }
        //不存在则添加
        HttpHeaderVo header = new HttpHeaderVo();
        header.setK(key);
        header.setV(value);
        headers.add(header);
    }

    public String getHeader(String key) {
        for (var it : headers) {
            if (it.getK().equalsIgnoreCase(key)) {
                return it.getV();
            }
        }
        return null;
    }

    /**
     * 设置主机名与协议 例如:http://www.baidu.com
     *
     * @param host 主机名与协议
     */
    public void setHost(String host) {
        this.url = replaceHostAndScheme(this.url, host);
    }

    /**
     * 获取请求构建器
     *
     * @return 请求构建器
     */
    public HttpRequest.Builder getBuilder() {
        HttpRequest.Builder builder = HttpRequest.newBuilder();
        builder.uri(URI.create(url));
        builder.method(method, HttpRequest.BodyPublishers.ofByteArray(body));
        builder.timeout(Duration.ofSeconds(3));

        for (var it : headers) {
            builder.header(it.getK(), it.getV());
        }

        return builder;
    }


}
