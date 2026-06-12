package com.ksptool.bio.commons.model;


import com.ksptool.bio.biz.drive.service.EntryAccessService;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublisher;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.UUID;

/**
 * 请求转发器
 */
public class HttpRelay {

    private final EntryAccessService entryAccessService;

    private final HttpClient client;

    public HttpRelay(EntryAccessService entryAccessService) {
        this.entryAccessService = entryAccessService;
        this.client = HttpClient.newBuilder().build();
    }

    /**
     * 发送请求
     *
     * @param schema 请求模式
     * @return 响应结果
     * @throws Exception 异常
     */
    public HttpRelayResponse sendRequest(HttpRelaySchema schema) throws Exception {

        HttpRequest.Builder builder = HttpRequest.newBuilder();

        //处理URL与查询参数
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString(schema.getUrl());
        uriBuilder.queryParams(RelayParam.toMultiValueMap(schema.getQueryParams()));
        builder.uri(uriBuilder.build().toUri());

        //处理请求方法与请求体
        var bodyPublisher = getBodyPublisher(schema.getBody());
        builder.method(schema.getMethodString(), bodyPublisher.getTarget());

        //处理请求头
        for (var header : schema.getHeaders()) {

            //过滤空请求头
            if (StringUtils.isAllBlank(header.getK(), header.getV())) {
                continue;
            }

            //跳过那些未启用的请求头
            if (!header.getE()) {
                continue;
            }

            //自动计算请求头值
            builder.header(header.getK(), computeHeaderValue(uriBuilder, header, bodyPublisher));

        }

        //发送请求
        var response = client.send(builder.build(), HttpResponse.BodyHandlers.ofInputStream());
        return new HttpRelayResponse(response);
    }


    /**
     * 计算请求头值
     *
     * @param uriBuilder    URI构建器
     * @param header        请求头
     * @param bodyPublisher 请求体发布者
     * @return 请求头值
     */
    private String computeHeaderValue(UriComponentsBuilder uriBuilder, RelayHeader header, HttpBodyPublisher bodyPublisher) {

        if (!header.getA()) {
            return header.getV();
        }

        var _hk = header.getK().toLowerCase();

        if (_hk.equals("eas-token")) {
            return UUID.randomUUID().toString();
        }

        if (_hk.equals("content-type")) {

            if (bodyPublisher.getSource().getKind() == 1) {
                return bodyPublisher.getContentType();
            }

            return bodyPublisher.getSource().getContentType();
        }

        if (_hk.equals("content-length")) {
            return String.valueOf(bodyPublisher.getContentLength());
        }

        if (_hk.equals("host")) {
            return uriBuilder.build().getHost();
        }

        return header.getV();
    }


    /**
     * 获取BodyPublisher
     *
     * @param body 请求模式
     * @return 请求体发布者
     */
    private HttpBodyPublisher getBodyPublisher(RelayBody body) throws Exception {

        //0:空 1:form-data 2:raw-text 3:raw-javascription 4:raw-json 5:raw-html 6:raw-xml 7:binary 8:x-www-form-urlencoded

        //空请求体
        if (body.getKind() == 0) {
            return new HttpBodyPublisher(body, BodyPublishers.noBody(), null, 0);
        }

        //form-data请求体
        if (body.getKind() == 1) {

            var multipart = new HttpRelayMultipartFile();

            //优先处理文本类型参数
            for (var it : body.getFormData()) {
                if (it.isText()) {
                    multipart.addText(it.getK(), it.getV());
                }
            }

            //处理文件类型参数
            var fileIds = new ArrayList<Long>();

            //先搜集表单中所有的文件IDS
            for (var it : body.getFormData()) {
                if (it.isFile()) {
                    fileIds.addAll(it.toFileIds());
                }
            }

            //根据IDS查询文件
            var fileMap = entryAccessService.getEntryFiles(fileIds);

            for (var it : body.getFormData()) {
                if (it.isFile()) {

                    var formFileIds = it.toFileIds();

                    for (var fileId : formFileIds) {
                        multipart.addFile(it.getK(), fileMap.get(fileId));
                    }

                }
            }

            return new HttpBodyPublisher(body, multipart.toBodyPublisher(), multipart.getContentType(), multipart.getContentLength());
        }

        //raw系列请求体 2~6
        if (body.getKind() >= 2 && body.getKind() <= 6) {
            var bytes = body.getRawData().getBytes(StandardCharsets.UTF_8);
            return new HttpBodyPublisher(body, BodyPublishers.ofByteArray(bytes), null, bytes.length);
        }

        //binary请求体
        if (body.getKind() == 7) {
            throw new IllegalArgumentException("暂不支持binary请求体");
        }

        //x-www-form-urlencoded请求体
        if (body.getKind() == 8) {

            var mvMap = RelayParam.toMultiValueMap(body.getFormDataUrlEncoded());

            // 自动处理 URL 编码和 & 符号拼接
            String queryString = UriComponentsBuilder.newInstance()
                    .queryParams(mvMap)
                    .build()
                    .toUriString();

            // 结果类似 "?key=val" 但作为 Body 发送 需要去掉开头的 "?" 否则会导致请求体格式错误
            if (queryString.startsWith("?")) {
                queryString = queryString.substring(1);
            }

            var bytes = queryString.getBytes(StandardCharsets.UTF_8);
            return new HttpBodyPublisher(body, BodyPublishers.ofByteArray(bytes), null, bytes.length);
        }

        throw new IllegalArgumentException("不支持的请求体类型: " + body.getKind());
    }

    @Getter
    private static class HttpBodyPublisher {

        private final RelayBody source;
        private final BodyPublisher target;
        private final long contentLength;
        private final String contentType;


        public HttpBodyPublisher(RelayBody source, BodyPublisher target, String contentType, long contentLength) {
            this.source = source;
            this.target = target;
            this.contentType = contentType;
            this.contentLength = contentLength;
        }

    }


}
