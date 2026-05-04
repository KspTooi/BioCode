package com.ksptool.bio.biz.auth.common;

import com.google.gson.Gson;
import com.ksptool.assembly.entity.web.Result;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NullMarked;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 认证入口点 用于处理认证失败和权限不足的情况
 * <p>
 * 这个类主要用于SpringSecurity认证失败和权限不足时返回标准化的JSON响应。
 * 因为SpringSecurity的默认认证失败和权限不足处理方式是重定向到登录页面，这不符合前后端分离的开发模式，所以需要这个类来处理。
 *
 * @author KspTool
 * @since 1.5.23(W).109
 */
@Component
public class JsonAuthEntryPoint implements AuthenticationEntryPoint, AccessDeniedHandler {

    private final Gson gson = new Gson();

    @NullMarked
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");

        var body = Result.error(401, "登录已过期或未登录");
        response.getWriter().write(gson.toJson(body));
    }

    @NullMarked
    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException, ServletException {

        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");

        var body = Result.error(403, "权限不足");
        response.getWriter().write(gson.toJson(body));
    }
}