package com.ksptool.bio.support.ratelimit;

import com.ksptool.assembly.entity.exception.BizException;
import com.ksptool.bio.commons.WebUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import static com.ksptool.bio.biz.auth.service.SessionService.sessionWithNullable;

@Aspect
@Component
public class RateLimitAspect {

    private final RateLimitCounter counter;

    public RateLimitAspect(RateLimitCounter counter) {
        this.counter = counter;
    }

    @Around("@annotation(rateLimit)")
    public Object around(ProceedingJoinPoint pjp, RateLimit rateLimit) throws Throwable {
        int maxCount = rateLimit.period();
        int windowSeconds = rateLimit.limit();

        if (maxCount < 1) {
            return pjp.proceed();
        }
        if (windowSeconds < 1) {
            return pjp.proceed();
        }

        MethodSignature sign = (MethodSignature) pjp.getSignature();

        String biz = rateLimit.biz();
        if (StringUtils.isBlank(biz)) {
            biz = sign.getDeclaringTypeName() + "#" + sign.getName();
        }

        String scopeValue = resolveScopeValue(rateLimit.scope());

        long now = System.currentTimeMillis() / 1000;
        long bucket = now / windowSeconds;

        String cacheName = "rateLimit_" + windowSeconds;
        String key = "ratelimit:" + biz + ":" + rateLimit.scope().name() + ":" + scopeValue + ":" + bucket;

        long count;
        try {
            count = counter.incrementAndGet(cacheName, key, windowSeconds + 1L);
        } catch (Exception ex) {
            if (rateLimit.failOpen()) {
                return pjp.proceed();
            }
            throw new BizException("限流存储异常");
        }

        if (count <= maxCount) {
            return pjp.proceed();
        }

        throw new BizException("请求过于频繁，请稍后重试");
    }

    private String resolveScopeValue(RateLimitScope scope) {
        if (scope == RateLimitScope.GLOBAL) {
            return "global";
        }

        if (scope == RateLimitScope.IP_ADDRESS) {
            HttpServletRequest req = currentRequest();
            if (req == null) {
                return "unknown";
            }
            return WebUtils.getIpAddr(req);
        }

        var session = sessionWithNullable();
        if (session == null) {
            return "anonymous";
        }
        if (session.getUserId() == null) {
            return "anonymous";
        }
        return String.valueOf(session.getUserId());
    }

    private HttpServletRequest currentRequest() {
        var attrs = RequestContextHolder.getRequestAttributes();
        if (!(attrs instanceof ServletRequestAttributes sra)) {
            return null;
        }
        return sra.getRequest();
    }
}