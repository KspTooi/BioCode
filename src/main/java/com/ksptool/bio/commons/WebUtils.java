package com.ksptool.bio.commons;

import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;

public class WebUtils {

    /**
     * 获取Cookie值
     *
     * @param request    请求
     * @param cookieName Cookie名称
     * @return Cookie值 如果Cookie不存在，则返回null
     */
    public static String getCookieValue(HttpServletRequest request, String cookieName) {
        var cookies = request.getCookies();
        if (cookies != null) {
            for (var cookie : cookies) {
                if (cookie.getName().equals(cookieName)) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    /**
     * 获取Authentication Bearer Token中的sessionId
     *
     * @param request 请求
     * @return Authentication Bearer Token中的sessionId 不存在或格式不正确，则返回null
     */
    public static String getAuthenticationBearerSessionId(HttpServletRequest request) {

        var header = request.getHeader("Authorization");

        if (StringUtils.isBlank(header)) {
            return null;
        }

        if (!header.startsWith("Bearer ")) {
            return null;
        }

        var sessionId = header.substring(7);

        if (StringUtils.isBlank(sessionId)) {
            return null;
        }

        return sessionId;
    }

    /**
     * 获取Authentication PAT Token
     *
     * @param request 请求
     * @return PAT Token，不存在或格式不正确则返回null
     */
    public static String getAuthenticationPatToken(HttpServletRequest request) {
        var header = request.getHeader("Authorization");
        if (StringUtils.isBlank(header)) {
            return null;
        }
        if (!header.startsWith("PAT ")) {
            return null;
        }
        var token = header.substring(4);
        if (StringUtils.isBlank(token)) {
            return null;
        }
        return token;
    }

    /*
     * 获取IP地址
     * @param request 请求
     * @return IP地址
     */
    public static String getIpAddr(HttpServletRequest request) {

        if (request == null) {
            return "unknown";
        }

        String ip = request.getHeader("X-REAL-IP");

        if (StringUtils.isNotBlank(ip)) {
            return ip;
        }

        return request.getRemoteAddr();
    }


}
