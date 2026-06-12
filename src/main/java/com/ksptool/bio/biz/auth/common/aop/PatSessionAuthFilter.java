package com.ksptool.bio.biz.auth.common.aop;

import com.ksptool.assembly.entity.exception.BizException;
import com.ksptool.bio.biz.auth.model.basicpat.BasicPatPo;
import com.ksptool.bio.biz.auth.model.auth.AuthUserSession;
import com.ksptool.bio.biz.auth.service.AuthUserDetailsService;
import com.ksptool.bio.biz.auth.service.BasicPatService;
import com.ksptool.bio.biz.auth.service.SessionService;
import com.ksptool.bio.biz.core.repository.UserRepository;
import com.ksptool.bio.commons.WebUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * PAT 虚拟会话认证过滤器
 * 拦截 Authorization: PAT usk-xxx 请求，验证后改写为 Bearer usk-xxx 移交给 USAF 处理
 *
 * @author KspTool
 * @since 1.7.5(E).1
 */
@Component
public class PatSessionAuthFilter extends OncePerRequestFilter {

    @Autowired
    private SessionService sessionService;

    @Autowired
    private BasicPatService basicPatService;

    @Autowired
    private AuthUserDetailsService authUserDetailsService;

    @Autowired
    private UserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        var patToken = WebUtils.getAuthenticationPatToken(request);
        if (patToken == null) {
            chain.doFilter(request, response);
            return;
        }

        try {
            var sessionPo = sessionService.getSessionBySessionId(patToken);
            if (sessionPo.isExpired()) {
                throw new BizException("PAT虚拟会话已过期，准备重连。");
            }
        } catch (BizException e) {
            try {
                BasicPatPo pat = basicPatService.validatePat(patToken);
                var userPo = userRepository.findById(pat.getUserId())
                        .orElseThrow(() -> new BizException("用户不存在"));
                var aus = (AuthUserSession) authUserDetailsService.loadUserByUsername(userPo.getUsername());
                sessionService.createPatSession(aus, patToken);
            } catch (Exception ex) {
                chain.doFilter(request, response);
                return;
            }
        }

        var wrapped = new HttpServletRequestWrapper(request) {
            @Override
            public String getHeader(String name) {
                if ("Authorization".equalsIgnoreCase(name)) {
                    return "Bearer " + patToken;
                }
                return super.getHeader(name);
            }
        };
        chain.doFilter(wrapped, response);
    }
}
