package com.ksptool.bio.biz.aacp.controller;

import com.google.gson.Gson;
import com.ksptool.assembly.entity.exception.BizException;
import com.ksptool.bio.biz.aacp.commons.McpClientSession;
import com.ksptool.bio.biz.aacp.commons.McpParser;
import com.ksptool.bio.biz.aacp.commons.providerclient.OpenAiDto;
import com.ksptool.bio.biz.aacp.repository.ModelRepository;
import com.ksptool.bio.biz.aacp.repository.ProviderRepository;
import com.ksptool.bio.biz.aacp.service.AacpAccessService;
import com.ksptool.bio.biz.auth.common.DynamicGlobalWhiteManager;
import com.ksptool.bio.commons.WebUtils;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;


@Slf4j
@RestController
@RequestMapping({"/aacp", "/v1"})
@Tag(name = "AACP访问端点", description = "该端点为AACP协议的访问端点，用于接收J-RPC协议的请求")
public class AacpAccessController {

    private final Gson gson = new Gson();

    @Autowired
    private AacpAccessService aacpAccessService;

    @Autowired
    private DynamicGlobalWhiteManager dgwm;

    @Autowired
    private ModelRepository mRepository;

    @Autowired
    private ProviderRepository pRepository;


    @EventListener(ApplicationReadyEvent.class)
    public void init() {
        dgwm.getWhiteListMatchers().add(PathPatternRequestMatcher.pathPattern("/aacp/upstream/**"));
        dgwm.getWhiteListMatchers().add(PathPatternRequestMatcher.pathPattern("/aacp/inbound"));
        dgwm.getWhiteListMatchers().add(PathPatternRequestMatcher.pathPattern("/v1/chat/completions"));
    }

    /**
     * 建立 SSE 连接
     */
    @GetMapping(value = "/upstream/{code}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter upstream(@PathVariable("code") String code) throws BizException {
        //SSE 长连接不得持有 JDBC 连接,读库已下沉到 Service 的工作线程执行,请求线程全程不绑定 EntityManager/连接,规避 OSIV 占用 HikariCP
        SseEmitter emitter = new SseEmitter(3600000L);
        aacpAccessService.createSession(code, emitter);
        return emitter;
    }

    /**
     * 处理 JSON-RPC 请求
     */
    @PostMapping(value = "/inbound", consumes = "application/json")
    public void inbound(@RequestParam("sessionId") String sessionId, @RequestBody String rawBody) {

        McpClientSession session = aacpAccessService.getSession(sessionId);
        if (session == null) {
            throw new RuntimeException("Session not found");
        }

        var p = McpParser.of(rawBody);
        var result = aacpAccessService.inbound(session, p.getInput());

        try {
            session.getEmitter().send(SseEmitter.event().data(gson.toJson(result)));
        } catch (IOException e) {
        }
    }

    @PostMapping(value = "/chat/completions", consumes = "application/json")
    public Object chatCompletion(HttpServletRequest hsr, @RequestBody OpenAiDto dto) throws Exception {

        //从请求头中获取sessionId
        var sessionId = WebUtils.getAuthenticationBearerSessionId(hsr);

        //获取模型变体名字
        var modelName = dto.getModel();

        var model = mRepository.getModelByNameAndAppkey(modelName, sessionId);

        if (model == null) {
            throw new BizException("模型不存在");
        }

        //获取该模型的第一个可用供应商
        var provider = pRepository.getFirstProviderByModelId(model.getId());

        if (provider == null) {
            throw new BizException("该模型没有可用供应商");
        }

        //准备参数
        var url = provider.getApiHost() + provider.getApiUrl();
        var token = provider.getApiKey();
        var providerModelCode = model.getCode();
        dto.setModel(providerModelCode);

        var isStream = Boolean.TRUE.equals(dto.getStream());
        dto.setStream(isStream);
        var requestBody = gson.toJson(dto);
        var httpClient = HttpClient.newHttpClient();
        var httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", "Bearer " + token)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        if (isStream) {
            SseEmitter emit = new SseEmitter(3600000L);
            Thread.ofVirtual().start(() -> {
                try {
                    var response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofLines());
                    if (response.statusCode() != 200) {
                        log.error("上游API返回错误 status={}", response.statusCode());
                        emit.send(SseEmitter.event().data(gson.toJson(Map.of(
                                "error", "上游API返回错误: " + response.statusCode()))));
                        emit.complete();
                        return;
                    }
                    response.body().forEach(line -> {
                        if (line == null || line.isBlank()) {
                            return;
                        }
                        if (line.startsWith("data: ")) {
                            var data = line.substring(6).trim();
                            if ("[DONE]".equals(data)) {
                                return;
                            }
                            try {
                                emit.send(SseEmitter.event().data(data));
                            } catch (IOException e) {
                                throw new UncheckedIOException(e);
                            }
                        }
                    });
                    emit.complete();
                } catch (UncheckedIOException e) {
                    log.info("SSE客户端断开连接");
                } catch (Exception e) {
                    log.error("转发请求时发生错误", e);
                    try {
                        emit.send(SseEmitter.event().data(gson.toJson(Map.of(
                                "error", e.getMessage()))));
                        emit.completeWithError(e);
                    } catch (IOException ex) {
                    }
                }
            });
            return emit;
        }

        var response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            return ResponseEntity.status(response.statusCode())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(gson.toJson(Map.of("error", "上游API返回错误: " + response.statusCode())));
        }
        var body = response.body();
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(body);
    }

}
