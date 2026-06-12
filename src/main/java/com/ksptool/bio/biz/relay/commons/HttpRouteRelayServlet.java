package com.ksptool.bio.commons.aop.relay;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.ksptool.bio.commons.utils.GsonUtils;
import com.ksptool.bio.biz.relay.model.relayserver.GetRelayServerDetailsVo;
import com.ksptool.bio.biz.relay.model.relayserver.RelayServerPo;
import com.ksptool.bio.biz.relay.model.request.RequestPo;
import com.ksptool.bio.biz.relay.service.RequestService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;

@Slf4j
public class HttpRouteRelayServlet extends HttpServlet {

    //hop-by-hop请求头
    private static final Set<String> HOP_BY_HOP_HEADERS = new HashSet<>();
    //不需要发送请求体的请求方法
    private static final Set<String> METHODS_WITHOUT_BODY = new HashSet<>();

    static {
        METHODS_WITHOUT_BODY.add("GET");
        METHODS_WITHOUT_BODY.add("DELETE");
        METHODS_WITHOUT_BODY.add("HEAD");
        METHODS_WITHOUT_BODY.add("OPTIONS");
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

    //HTTP客户端
    private final HttpClient httpClient;
    //Gson
    private final Gson gson;
    //中继通道
    private final GetRelayServerDetailsVo relayServer;
    private final RequestService requestService;

    /**
     * 构造函数
     *
     * @param relayServer    中继通道
     * @param httpClient     HTTP客户端
     * @param gson           Gson
     * @param requestService 请求服务
     */
    public HttpRouteRelayServlet(GetRelayServerDetailsVo relayServer, HttpClient httpClient, Gson gson, RequestService requestService) {
        this.relayServer = relayServer;
        this.httpClient = httpClient;
        this.gson = gson;
        this.requestService = requestService;
    }

    @Override
    protected void service(HttpServletRequest hsr, HttpServletResponse hsrp) throws ServletException, IOException {

        RelayServerPo relayServerPo = new RelayServerPo();
        relayServerPo.setId(relayServer.getId());

        //构建请求记录
        RequestPo request = new RequestPo();
        request.setRelayServer(relayServerPo);
        request.setRequestId(UUID.randomUUID().toString());
        request.setMethod(hsr.getMethod());
        request.setUrl(replaceHostAndScheme(hsr.getRequestURL().toString(), relayServer.getForwardUrl()));
        request.setSource(hsr.getRemoteAddr());
        request.setRequestHeaders(getHeaderJson(hsr));
        request.setRequestBodyLength(hsr.getContentLength());
        var contentType = hsr.getContentType();
        request.setRequestBodyType(contentType);

        if (contentType == null) {
            request.setRequestBodyType("UNKNOWN");
        }

        request.setRequestBody("{}");
        request.setResponseHeaders("{}");
        request.setResponseBodyLength(0);
        request.setResponseBodyType("?");
        request.setResponseBody("{}");
        request.setStatusCode(-1); //-1为请求失败
        request.setRedirectUrl(null);
        request.setStatus(1); //0:正常 1:HTTP失败 2:业务失败 3:连接超时
        request.setRequestTime(LocalDateTime.now());
        request.setResponseTime(null);

        try {
            byte[] requestBody = readRequestBody(hsr);

            //如果是JSON请求则需要记录请求体
            if (contentType != null && contentType.contains("application/json")) {
                request.setRequestBody(new String(requestBody, StandardCharsets.UTF_8));
            }

            //中继请求并获取响应
            HttpResponse<byte[]> upstream = doRequest(hsr, requestBody);
            byte[] responseBody = upstream.body();


            request.setResponseHeaders(getHeaderJson(upstream));
            request.setResponseBodyLength(responseBody.length);

            //获取响应头中的content-type
            var retContentType = upstream.headers().firstValue("content-type").orElse(null);
            request.setResponseBodyType(retContentType);
            if (retContentType == null) {
                request.setResponseBodyType("UNKNOWN");
            }

            //如果是JSON响应则需要记录响应体
            if (retContentType != null && retContentType.contains("application/json")) {
                request.setResponseBody(new String(responseBody, StandardCharsets.UTF_8));
            }

            request.setStatusCode(upstream.statusCode());

            //如果响应头中包含location则需要记录重定向URL
            String location = upstream.headers().firstValue("Location").orElse(null);

            //如果重定向的主机是中继服务器的目标地址 则将其变更为中继入口
            if (location != null) {
                //如果重定向覆写为1 则覆写重定向URL
                if (relayServer.getOverrideRedirect() == 1) {
                    var oldLocation = location;
                    location = replaceHostAndScheme(location, relayServer.getOverrideRedirectUrl());
                    log.info("中继通道:{} 已将重定向URL覆写为:{} 原重定向URL:{}", relayServer.getName(), location, oldLocation);
                }
                request.setRedirectUrl(location);
            }

            request.setStatus(0); //0:正常 1:HTTP失败 2:业务失败 3:连接超时
            request.setResponseTime(LocalDateTime.now());

            //如果请求ID策略为1 则尝试从响应头中获取请求ID
            if (relayServer.getRequestIdStrategy() == 1) {
                String requestId = upstream.headers().firstValue(relayServer.getRequestIdHeaderName()).orElse(null);
                if (requestId != null) {
                    request.setRequestId(requestId);
                }
                if (requestId == null) {
                    log.warn("中继通道:{} 当前配置了从响应头中获取请求ID,但未能正常获取,已生成随机请求ID:{}", relayServer.getName(), request.getRequestId());
                }
            }

            //如果业务错误策略为1 则尝试从响应体中获取业务错误码
            if (relayServer.getBizErrorStrategy() == 1) {
                if (request.getResponseBodyType().contains("application/json")) {

                    JsonElement jsonTree = gson.fromJson(new String(responseBody, StandardCharsets.UTF_8), JsonElement.class);

                    String successCode = relayServer.getBizSuccessCodeValue();
                    String bizErrorCode = GsonUtils.getFromPath(jsonTree, relayServer.getBizErrorCodeField());

                    //无法获取到错误码值 直接判定业务错误
                    if (bizErrorCode == null) {
                        request.setStatus(2); //0:正常 1:HTTP失败 2:业务失败 3:连接超时
                    }
                    if (bizErrorCode != null) {
                        if (bizErrorCode.equals(successCode)) {
                            request.setStatus(0); //0:正常 1:HTTP失败 2:业务失败 3:连接超时
                        }
                        if (!bizErrorCode.equals(successCode)) {
                            request.setStatus(2); //0:正常 1:HTTP失败 2:业务失败 3:连接超时
                        }
                    }
                }

            }

            //如果有X-Real-IP则将源设置为X-Real-IP
            if (hsr.getHeader("X-Real-IP") != null) {
                request.setSource(hsr.getHeader("X-Real-IP"));
            }

            //中继响应
            doResponse(hsrp, upstream, responseBody);

        } catch (ConnectException ex) {
            log.error(ex.getMessage(), ex);
            hsrp.setStatus(HttpServletResponse.SC_BAD_GATEWAY);
            writeBytes(hsrp, ("中继错误,无法建立中继通道 - 目标服务器:'" + relayServer.getForwardUrl() + "'无响应或已超时").getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            hsrp.setStatus(HttpServletResponse.SC_BAD_GATEWAY);
            writeBytes(hsrp, ("中继错误: " + e.getMessage()).getBytes(StandardCharsets.UTF_8));
        } finally {
            requestService.commitRequest(request);
        }


    }

    /**
     * 处理请求
     *
     * @param hsr         请求
     * @param requestBody 请求体
     * @return 响应
     */
    private HttpResponse<byte[]> doRequest(HttpServletRequest hsr, byte[] requestBody) throws Exception {

        //构建目标URI
        var targetUri = buildTargetUri(hsr);
        var method = hsr.getMethod();

        //构建目标请求
        HttpRequest.Builder builder = HttpRequest.newBuilder(targetUri);
        copyRequestHeaders(hsr, builder);

        //无需发送请求体的请求
        if (METHODS_WITHOUT_BODY.contains(method)) {
            builder.method(method, HttpRequest.BodyPublishers.noBody());
        }
        //构建请求体
        if (!METHODS_WITHOUT_BODY.contains(method)) {
            builder.method(method, HttpRequest.BodyPublishers.ofByteArray(requestBody));
        }

        return httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofByteArray());
    }

    /**
     * 处理响应
     *
     * @param hsrp     响应
     * @param upstream 响应
     * @param body     响应体
     */
    private void doResponse(HttpServletResponse hsrp, HttpResponse<byte[]> upstream, byte[] body) throws Exception {
        hsrp.setStatus(upstream.statusCode());
        copyResponseHeaders(upstream, hsrp);

        //如果响应头中包含Location则需要覆写重定向URL
        if (upstream.headers().firstValue("Location").isPresent()) {
            if (relayServer.getOverrideRedirect() == 1) {
                String location = upstream.headers().firstValue("Location").get();
                var oldLocation = location;
                location = replaceHostAndScheme(location, relayServer.getOverrideRedirectUrl());
                hsrp.setHeader("Location", location);
                log.info("中继通道:{} 已将重定向URL覆写为:{} 原重定向URL:{}", relayServer.getName(), location, oldLocation);
            }
        }

        if (body != null && body.length > 0) {
            writeBytes(hsrp, body);
            return;
        }
        hsrp.flushBuffer();
    }


    /**
     * 复制请求头到目标请求
     *
     * @param request 请求
     * @param builder 请求构建器
     */
    private void copyRequestHeaders(HttpServletRequest request, HttpRequest.Builder builder) {
        Enumeration<String> headerNames = request.getHeaderNames();
        if (headerNames == null) {
            return;
        }
        while (headerNames.hasMoreElements()) {
            String name = headerNames.nextElement();
            if (StringUtils.isBlank(name)) {
                continue;
            }
            String lower = name.toLowerCase();
            if (HOP_BY_HOP_HEADERS.contains(lower)) {
                continue;
            }
            Enumeration<String> values = request.getHeaders(name);
            while (values.hasMoreElements()) {
                String value = values.nextElement();
                if (StringUtils.isBlank(value)) {
                    continue;
                }
                builder.header(name, value);
            }
        }
    }

    /**
     * 复制响应头到目标响应
     *
     * @param upstream 上游响应
     * @param response 目标响应
     */
    private void copyResponseHeaders(HttpResponse<byte[]> upstream, HttpServletResponse response) {
        for (var entry : upstream.headers().map().entrySet()) {
            String name = entry.getKey();
            if (StringUtils.isBlank(name)) {
                continue;
            }
            String lower = name.toLowerCase();
            if (HOP_BY_HOP_HEADERS.contains(lower)) {
                continue;
            }
            for (String value : entry.getValue()) {
                if (StringUtils.isBlank(value)) {
                    continue;
                }
                response.addHeader(name, value);
            }
        }
    }

    /**
     * 读取请求体
     *
     * @param request 请求
     * @return 请求体
     * @throws IOException
     */
    private byte[] readRequestBody(HttpServletRequest request) throws IOException {
        int length = request.getContentLength();
        if (length == 0) {
            return new byte[0];
        }
        if (length < 0) {
            try (InputStream in = request.getInputStream()) {
                return in.readAllBytes();
            }
        }
        byte[] buffer = new byte[length];
        try (InputStream in = request.getInputStream()) {
            int readTotal = 0;
            while (readTotal < length) {
                int r = in.read(buffer, readTotal, length - readTotal);
                if (r < 0) {
                    break;
                }
                readTotal += r;
            }
        }
        return buffer;
    }

    /**
     * 写入响应体
     *
     * @param response 响应
     * @param bytes    响应体
     * @throws IOException
     */
    private void writeBytes(HttpServletResponse response, byte[] bytes) throws IOException {
        if (bytes == null || bytes.length == 0) {
            response.flushBuffer();
            return;
        }
        response.setContentLength(bytes.length);
        try (OutputStream os = response.getOutputStream()) {
            os.write(bytes);
            os.flush();
        }
    }

    /**
     * 替换URL中的主机名、协议和端口号 例如:http://www.baidu.com/index.html 替换为 http://127.0.0.1:8080/index.html
     *
     * @param url       原始URL
     * @param targetUrl 目标URL
     * @return 替换后的URL
     */
    private String replaceHostAndScheme(String url, String targetUrl) {
        try {
            URI uri = new URI(url);
            URI targetUri = new URI(targetUrl);
            return new URI(targetUri.getScheme(), targetUri.getUserInfo(), targetUri.getHost(), targetUri.getPort(), uri.getPath(), uri.getQuery(), uri.getFragment()).toString();
        } catch (URISyntaxException e) {
            return url;
        }
    }

    public URI buildTargetUri(HttpServletRequest request) {
        String base = relayServer.getForwardUrl();
        if (StringUtils.isBlank(base)) {
            throw new IllegalStateException("未配置repeater.proxy_pass");
        }
        if (!StringUtils.startsWithIgnoreCase(base, "http://") && !StringUtils.startsWithIgnoreCase(base, "https://")) {
            base = "http://" + base;
        }

        StringBuilder url = new StringBuilder();
        url.append(base);
        url.append(request.getRequestURI());

        String query = request.getQueryString();
        if (StringUtils.isNotBlank(query)) {
            url.append('?').append(query);
        }
        return URI.create(url.toString());
    }


    /**
     * 获取请求头JSON
     *
     * @param hsr 请求
     * @return 请求头JSON 当获取失败时返回空JSON
     */
    public String getHeaderJson(HttpServletRequest hsr) {
        try {
            Map<String, String> requestHeaders = new HashMap<>();
            Enumeration<String> headerNames = hsr.getHeaderNames();
            while (headerNames.hasMoreElements()) {
                String name = headerNames.nextElement();
                requestHeaders.put(name, hsr.getHeader(name));
            }
            return gson.toJson(requestHeaders);
        } catch (Exception e) {
            return "{}";
        }
    }

    /**
     * 获取响应头JSON
     *
     * @param hr 响应
     * @return 响应头JSON 当获取失败时返回空JSON
     */
    public String getHeaderJson(HttpResponse<?> hr) {
        try {
            Map<String, String> responseHeaders = new HashMap<>();
            for (var entry : hr.headers().map().entrySet()) {
                String name = entry.getKey();
                if (StringUtils.isBlank(name)) {
                    continue;
                }
                String lower = name.toLowerCase();
                if (HOP_BY_HOP_HEADERS.contains(lower)) {
                    continue;
                }
                for (String value : entry.getValue()) {
                    if (StringUtils.isBlank(value)) {
                        continue;
                    }
                    responseHeaders.put(name, value);
                }
            }
            return gson.toJson(responseHeaders);
        } catch (Exception e) {
            return "{}";
        }
    }


}
