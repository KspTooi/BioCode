package com.ksptool.bio.biz.relay.service;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.ksptool.assembly.entity.exception.AuthException;
import com.ksptool.assembly.entity.exception.BizException;
import com.ksptool.assembly.entity.web.PageResult;
import com.ksptool.bio.biz.auth.service.AuthService;
import com.ksptool.bio.biz.auth.service.SessionService;
import com.ksptool.bio.biz.rdbg.commons.GsonUtils;
import com.ksptool.bio.biz.relay.model.replayrequest.*;
import com.ksptool.bio.biz.relay.model.request.RequestPo;
import com.ksptool.bio.biz.relay.repository.ReplayRequestRepository;
import com.ksptool.bio.biz.relay.repository.RequestRepository;
import io.micrometer.common.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
public class ReplayRequestService {

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

    @Autowired
    private ReplayRequestRepository replayRequestRepository;

    @Autowired
    private RequestRepository requestRepository;

    @Autowired
    private Gson gson;

    @Autowired
    private AuthService authService;
    @Autowired
    private SessionService sessionService;

    public void replayRequest(String requestId) throws Exception {

        RequestPo originalRequest = requestRepository.getByRequestId(requestId);

        if (originalRequest == null) {
            throw new BizException("原始请求不存在");
        }

        //获取原始请求URL
        String url = originalRequest.getUrl();

        //获取原始请求方法
        String method = originalRequest.getMethod();

        //获取原始请求头
        String headers = originalRequest.getRequestHeaders();

        //获取原始请求体
        String body = originalRequest.getRequestBody();

        String requestType = originalRequest.getRequestBodyType();

        //只能重放json请求
        if (!requestType.contains("application/json")) {
            throw new BizException("只能重放application/json请求");
        }

        //只支持post请求
        if (!method.equalsIgnoreCase("POST")) {
            throw new BizException("当前只支持重放POST请求");
        }

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest.Builder request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .method(method, HttpRequest.BodyPublishers.ofString(body))
                .timeout(Duration.ofSeconds(3));

        //将JSON格式的请求头处理为Map
        Map<String, String> headersMap = new HashMap<>();
        if (StringUtils.isNotBlank(headers)) {
            headersMap = gson.fromJson(headers, Map.class);
        }

        //将请求头添加到请求中
        for (Map.Entry<String, String> entry : headersMap.entrySet()) {
            if (HOP_BY_HOP_HEADERS.contains(entry.getKey().toLowerCase())) {
                continue;
            }
            request.header(entry.getKey(), entry.getValue());
        }

        //创建重放记录
        ReplayRequestPo replayRequestPo = new ReplayRequestPo();
        replayRequestPo.setRelayServer(originalRequest.getRelayServer());
        replayRequestPo.setOriginalRequest(originalRequest);
        replayRequestPo.setUser(sessionService.requireUser());
        replayRequestPo.setRequestId(UUID.randomUUID().toString());
        replayRequestPo.setMethod(method);
        replayRequestPo.setUrl(url);
        replayRequestPo.setSource("EAS服务");
        replayRequestPo.setRequestHeaders(headers);
        replayRequestPo.setRequestBodyLength(body.length());
        replayRequestPo.setRequestBodyType(requestType);
        replayRequestPo.setRequestBody(body);
        replayRequestPo.setResponseHeaders(null);
        replayRequestPo.setResponseBodyLength(0);
        replayRequestPo.setResponseBodyType(null);
        replayRequestPo.setResponseBody(null);
        replayRequestPo.setStatusCode(-1); //-1为请求失败
        replayRequestPo.setRedirectUrl(null);
        replayRequestPo.setStatus(3); //0:正常 1:HTTP失败 2:业务失败 3:连接超时
        replayRequestPo.setRequestTime(LocalDateTime.now());
        replayRequestPo.setResponseTime(null);


        try {
            //发送请求
            HttpResponse<String> response = client.send(request.build(), HttpResponse.BodyHandlers.ofString());

            //解析响应
            String responseBody = response.body();
            replayRequestPo.setResponseBodyLength(responseBody.length());
            replayRequestPo.setResponseBodyType(response.headers().firstValue("Content-Type").orElse(""));
            replayRequestPo.setResponseBody(responseBody);
            replayRequestPo.setStatusCode(response.statusCode());
            replayRequestPo.setRedirectUrl(response.headers().firstValue("Location").orElse(null));
            replayRequestPo.setStatus(0);
            replayRequestPo.setResponseTime(LocalDateTime.now());

            //如果请求ID策略为1 则尝试从响应头中获取请求ID
            if (originalRequest.getRelayServer().getRequestIdStrategy() == 1) {
                String replayRequestId = response.headers().firstValue(originalRequest.getRelayServer().getRequestIdHeaderName()).orElse(null);
                if (replayRequestId != null) {
                    replayRequestPo.setRequestId(replayRequestId);
                }
                if (requestId == null) {
                    log.warn("中继通道:{} 当前配置了从响应头中获取请求ID,但未能正常获取,已生成随机请求ID:{}", originalRequest.getRelayServer().getName(), replayRequestPo.getRequestId());
                }
            }

            //如果业务错误策略为1 则尝试从响应体中获取业务错误码
            if (originalRequest.getRelayServer().getBizErrorStrategy() == 1) {
                if (replayRequestPo.getResponseBodyType().contains("application/json")) {

                    JsonElement jsonTree = gson.fromJson(responseBody, JsonElement.class);

                    String successCode = originalRequest.getRelayServer().getBizSuccessCodeValue();
                    String bizErrorCode = GsonUtils.getFromPath(jsonTree, originalRequest.getRelayServer().getBizErrorCodeField());

                    //无法获取到错误码值 直接判定业务错误
                    if (bizErrorCode == null) {
                        replayRequestPo.setStatus(2); //0:正常 1:HTTP失败 2:业务失败 3:连接超时
                    }
                    if (bizErrorCode != null) {
                        if (bizErrorCode.equals(successCode)) {
                            replayRequestPo.setStatus(0); //0:正常 1:HTTP失败 2:业务失败 3:连接超时
                        }
                        if (!bizErrorCode.equals(successCode)) {
                            replayRequestPo.setStatus(2); //0:正常 1:HTTP失败 2:业务失败 3:连接超时
                        }
                    }
                }

            }


        } catch (IOException | InterruptedException e) {
            throw new BizException("发送请求失败", e);
        } finally {
            //保存重放记录
            replayRequestRepository.save(replayRequestPo);
        }

    }

    public GetOriginRequestVo getOriginRequest(String requestId) throws Exception {
        GetOriginRequestVo originalRequest = requestRepository.getRequestByRequestId(requestId);
        if (originalRequest == null) {
            throw new BizException("原始请求不存在");
        }
        return originalRequest;
    }


    /**
     * 获取请求列表
     *
     * @param dto 请求列表查询条件
     * @return 请求列表
     */
    public PageResult<GetReplayRequestListVo> getReplayRequestList(GetReplayRequestListDto dto) throws AuthException {
        Page<GetReplayRequestListVo> page = replayRequestRepository.getReplayRequestList(dto, SessionService.session().getUserId(), dto.pageRequest());
        return PageResult.success(page.getContent(), page.getTotalElements());
    }

    /**
     * 获取请求详情
     *
     * @param id 请求ID
     * @return 请求详情
     */
    public GetReplayRequestDetailsVo getReplayRequestDetails(Long id) {
        ReplayRequestPo item = replayRequestRepository.findById(id).orElse(null);
        if (item == null) {
            return null;
        }
        GetReplayRequestDetailsVo vo = new GetReplayRequestDetailsVo();
        vo.setId(item.getId());
        vo.setRequestId(item.getRequestId());
        vo.setMethod(item.getMethod());
        vo.setUrl(item.getUrl());
        vo.setSource(item.getSource());
        vo.setStatusCode(item.getStatusCode());
        vo.setRequestBody(item.getRequestBody());
        vo.setResponseBody(item.getResponseBody());
        vo.setRequestHeaders(item.getRequestHeaders());
        vo.setResponseHeaders(item.getResponseHeaders());
        vo.setRequestBodyLength(item.getRequestBodyLength());
        vo.setResponseBodyLength(item.getResponseBodyLength());
        vo.setRequestBodyType(item.getRequestBodyType());
        vo.setResponseBodyType(item.getResponseBodyType());
        vo.setRedirectUrl(item.getRedirectUrl());
        vo.setStatus(item.getStatus());
        vo.setRequestTime(item.getRequestTime());
        vo.setResponseTime(item.getResponseTime());
        return vo;
    }


}
