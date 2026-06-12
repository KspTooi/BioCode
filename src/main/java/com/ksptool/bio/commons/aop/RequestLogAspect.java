package com.ksptool.bio.commons.aop;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.ksptool.bio.biz.core.common.config.gson.LocalDateAdapter;
import com.ksptool.bio.biz.rdbg.commons.GsonUtils;
import com.ksptool.bio.commons.annotation.PrintLog;
import com.ksptool.bio.commons.config.LocalDateTimeAdapter;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.lang.annotation.Annotation;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * 用于控制台打印出请求访问日志
 */
@Aspect
@Component
public class RequestLogAspect {

    private static final Logger log = LoggerFactory.getLogger(RequestLogAspect.class);
    private static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
            .create();

    @Pointcut("@annotation(com.ksptool.bio.commons.annotation.PrintLog)")
    public void printPointCut() {
    }

    @Pointcut("@within(com.ksptool.bio.commons.annotation.PrintLog)")
    public void printPointCutClass() {
    }

    @Around("printPointCut() || printPointCutClass()")
    public Object doAround(ProceedingJoinPoint pjp) throws Throwable {

        HttpServletRequest hsr = null;
        List<String> sensitiveFields = new ArrayList<>();

        try {
            hsr = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();//获取request
            sensitiveFields.addAll(getSensitiveFields(pjp));
        } catch (Exception ex) {
            return pjp.proceed();
        }

        final var start = System.currentTimeMillis();
        final var method = hsr.getMethod();
        final var url = hsr.getRequestURI();
        final var source = hsr.getRemoteAddr();
        final var reqBody = getRequestBody(pjp, sensitiveFields);

        final var requestId = hsr.getHeader("gateway-request-id");
        var appendMsg = "";

        if (StringUtils.isNotBlank(requestId)) {
            appendMsg = " 网关请求ID:" + requestId + " ";
        }

        try {
            Object ret = pjp.proceed();
            final var respBody = getResponseBody(ret, sensitiveFields);
            final var end = System.currentTimeMillis();
            final var cost = end - start;
            final var isTriggerSensitive = !sensitiveFields.isEmpty();
            log.info("请求=>{}[{}]{}路径:{} 载荷:{} 响应:{} 耗时:{} ms 已脱敏:{}", url, method, appendMsg, source, reqBody, respBody, cost, isTriggerSensitive);
            return ret;
        } catch (Exception ex) {
            final var respBody = ex.getMessage();
            final var end = System.currentTimeMillis();
            final var cost = end - start;
            final var isTriggerSensitive = !sensitiveFields.isEmpty();
            log.info("请求=>{}[{}]{}路径:{} 载荷:{} 响应:{} 耗时:{} ms 已脱敏:{}", url, method, appendMsg, source, reqBody, respBody, cost, isTriggerSensitive);
            throw ex;
        }
    }

    /**
     * 获取响应体
     *
     * @param ret             响应体
     * @param sensitiveFields 敏感字段列表
     * @return 过滤后的响应体
     */
    private String getResponseBody(Object ret, List<String> sensitiveFields) {
        if (ret == null) {
            return "无";
        }

        if (ret instanceof ResponseEntity<?> e) {
            return "ResponseEntity:" + e.getStatusCode();
        }

        if (ret instanceof SseEmitter) {
            return "[SSE 长连接流]";
        }

        JsonElement json = gson.toJsonTree(ret);
        return filterSensitiveFields(json, sensitiveFields).toString();
    }


    /**
     * 获取请求体
     *
     * @param pjp             切点
     * @param sensitiveFields 敏感字段列表
     * @return 过滤后的请求体
     */
    public String getRequestBody(ProceedingJoinPoint pjp, List<String> sensitiveFields) {

        Object[] para = pjp.getArgs();

        if (para == null || para.length < 1) {
            return "无";
        }

        MethodSignature sign = (MethodSignature) pjp.getSignature();
        Annotation[][] pAnno = sign.getMethod().getParameterAnnotations();

        if (pAnno.length < 1) {
            return "无";
        }

        //反射获取参数上首个RequestBody的序列
        int seq = -1;

        for (int a = 0; a < pAnno.length; a++) {

            for (int b = 0; b < pAnno[a].length; b++) {

                if (pAnno[a][b] instanceof RequestBody) {
                    seq = a;
                    break;
                }

            }

        }

        //没有获取到RequestBody序列
        if (seq < 0) {
            return "无";
        }

        try {
            JsonElement json = gson.toJsonTree(para[seq]);
            return filterSensitiveFields(json, sensitiveFields).toString();
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            return "获取失败";
        }

    }


    /**
     * 过滤敏感字段
     *
     * @param json            原始json
     * @param sensitiveFields 敏感字段列表
     * @return 过滤后的json
     */
    public JsonElement filterSensitiveFields(JsonElement json, List<String> sensitiveFields) {

        if (json == null || sensitiveFields == null || sensitiveFields.isEmpty()) {
            return json;
        }

        for (String field : sensitiveFields) {
            json = GsonUtils.replaceContent(json, field, "***");
        }

        return json;
    }


    /**
     * 获取方法上的敏感字段
     *
     * @param pjp 切点
     * @return 敏感字段列表
     */
    private List<String> getSensitiveFields(ProceedingJoinPoint pjp) {
        MethodSignature signature = (MethodSignature) pjp.getSignature();

        // 只检查方法上的注解
        PrintLog methodAnnotation = signature.getMethod().getAnnotation(PrintLog.class);

        if (methodAnnotation != null && methodAnnotation.sensitiveFields().length > 0) {
            return Arrays.asList(methodAnnotation.sensitiveFields());
        }

        return Collections.emptyList();
    }

}
