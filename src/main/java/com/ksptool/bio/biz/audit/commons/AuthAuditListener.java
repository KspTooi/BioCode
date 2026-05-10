package com.ksptool.bio.biz.audit.commons;

import com.ksptool.bio.biz.audit.service.AuditLoginService;
import com.ksptool.bio.biz.auth.common.exception.AuthUnavailableException;
import com.ksptool.bio.biz.auth.common.exception.RootUnavailableException;
import com.ksptool.bio.biz.auth.model.auth.AuthUserSession;
import com.ksptool.bio.biz.core.repository.UserRepository;
import com.ksptool.bio.commons.WebUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AbstractAuthenticationFailureEvent;
import org.springframework.security.authentication.event.AuthenticationFailureServiceExceptionEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * 用户登录审计监听器 用于记录登录审计日志
 * <p>
 * 这个类主要用于记录用户登录审计日志，包括登录成功和登录失败的情况。
 * 它会监听SpringSecurity的登录成功和登录失败事件，并记录相应的审计日志。
 *
 * @author KspTool
 * @since 1.5.23(W).109
 */
@Slf4j
@Component
public class AuthAuditListener {

    @Autowired
    private AuditLoginService auditLoginService;

    @Autowired
    private UserRepository userRepository;

    /**
     * 用户登录成功事件 用于记录登录审计日志
     *
     * @param event 事件
     */
    @EventListener(AuthenticationSuccessEvent.class)
    public void onSuccess(AuthenticationSuccessEvent event) {

        Authentication auth = event.getAuthentication();

        //获取登录成功的AUD
        AuthUserSession aud = (AuthUserSession) auth.getPrincipal();

        if (aud == null) {
            log.error("监听到登录成功事件 获取登录成功的AUD失败: authentication={}", auth);
            return;
        }

        //准备审计所需的参数
        var requestAttributes = RequestContextHolder.getRequestAttributes();

        var ip = "Unknown";
        var uaStr = "Unknown";

        if (requestAttributes instanceof ServletRequestAttributes) {
            HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
            ip = WebUtils.getIpAddr(request);
            uaStr = request.getHeader("User-Agent");
        }

        var userId = aud.getUserId();
        var username = aud.getUsername();
        var loginKind = 0; // 0:用户名密码
        var message = "登录成功";

        //记录登录审计日志
        auditLoginService.recordLoginAudit(userId, username, 0, message, ip, uaStr);
    }


    /**
     * 用户登录失败事件 用于记录登录审计日志
     *
     * @param event 事件
     */
    @EventListener(AuthenticationFailureServiceExceptionEvent.class)
    public void onFailure(AbstractAuthenticationFailureEvent event) {

        Authentication auth = event.getAuthentication();

        //准备审计所需的参数
        var requestAttributes = RequestContextHolder.getRequestAttributes();
        var ip = "Unknown";
        var uaStr = "Unknown";
        if (requestAttributes instanceof ServletRequestAttributes) {
            HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
            ip = WebUtils.getIpAddr(request);
            uaStr = request.getHeader("User-Agent");
        }
        var userId = 0L;
        var username = String.valueOf(auth.getPrincipal()); // 通常是用户名字符串
        var loginKind = 0; // 0:用户名密码
        var message = "用户密码错误";

        //查找一次用户
        if (userRepository.countByUsername(username) < 1) {
            message = "用户不存在";
        }

        //如果异常是租户不可用异常，则返回租户不可用异常信息
        if (event.getException().getCause() instanceof RootUnavailableException) {
            message = event.getException().getMessage();
        }

        //如果异常是认证不可用异常，则返回认证不可用异常信息
        if (event.getException().getCause() instanceof AuthUnavailableException) {
            message = event.getException().getMessage();
        }

        //记录登录审计日志
        auditLoginService.recordLoginAudit(userId, username, 1, message, ip, uaStr);
    }
}
