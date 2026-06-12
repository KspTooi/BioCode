package com.ksptool.bio.commons.utils;

import com.ksptool.bio.commons.routeselector.HttpRouteRule;
import jakarta.servlet.http.HttpServletRequest;

import java.util.Comparator;
import java.util.List;

/**
 * 路由选择器
 * 根据请求的URL和路由规则选择合适的目标服务器
 */
public class RouteSelector {

    //运行模式 DIRECT:直接模式 ROUTE:路由模式
    public String mode;

    //用于直接模式的目标URL
    public String targetUrl;

    //路由规则
    public List<HttpRouteRule> routeRules;

    public RouteSelector(String targetUrl) {
        if (targetUrl == null || targetUrl.isEmpty()) {
            throw new IllegalArgumentException("目标URL不能为空");
        }
        this.targetUrl = targetUrl;
        this.mode = "DIRECT";
    }

    public RouteSelector(List<HttpRouteRule> routeRules) {
        if (routeRules == null || routeRules.isEmpty()) {
            throw new IllegalArgumentException("路由规则不能为空");
        }
        this.routeRules = routeRules;
        this.mode = "ROUTE";

        //按权重升序排序
        this.routeRules.sort(Comparator.comparingInt(HttpRouteRule::getSeq));
    }

    /**
     * 获取请求的IP地址
     *
     * @param request 请求
     * @return IP地址
     */
    public static String getIpAddress(HttpServletRequest request) {
        String ip = request.getHeader("X-Real-IP");
        if (ip == null) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }

    /**
     * 选择目标服务器
     *
     * @return
     */
    public String selectTargetUrl() {
        if ("DIRECT".equals(mode)) {
            return "http://" + targetUrl;
        }

        if (routeRules == null || routeRules.isEmpty()) {
            return null;
        }

        for (HttpRouteRule rule : routeRules) {
            if (rule.isBreaked()) {
                continue;
            }

            if (rule.getMatchType() == 0) {
                rule.getHitCount().incrementAndGet();
                return "http://" + rule.getTargetHost() + ":" + rule.getTargetPort();
            }
        }

        return null;
    }

    /**
     * 选择目标服务器
     *
     * @param url
     * @param request 请求
     * @return
     */
    public String selectTargetUrl(HttpServletRequest request) {
        if ("DIRECT".equals(mode)) {
            return "http://" + targetUrl;
        }

        if (routeRules == null || routeRules.isEmpty()) {
            return null;
        }

        String clientIp = getIpAddress(request);

        for (HttpRouteRule rule : routeRules) {
            if (rule.isBreaked()) {
                continue;
            }

            if (rule.getMatchType() == 0) {
                rule.getHitCount().incrementAndGet();
                return "http://" + rule.getTargetHost() + ":" + rule.getTargetPort();
            }

            if (rule.getMatchType() == 1) {
                if (rule.getMatchOperator() == 0 && clientIp.equals(rule.getMatchValue())) {
                    rule.getHitCount().incrementAndGet();
                    return "http://" + rule.getTargetHost() + ":" + rule.getTargetPort();
                }
            }
        }

        return null;
    }

    /**
     * 重置所有主机的熔断状态
     */
    public void resetBreakStatus() {
        for (HttpRouteRule rule : routeRules) {
            rule.getIsBreaked().set(0);
        }
    }

    /**
     * 重置指定主机的熔断状态
     *
     * @param host 主机
     * @param port 端口
     */
    public void resetBreakStatus(String host, Integer port) {
        for (HttpRouteRule rule : routeRules) {
            if (rule.getTargetHost().equals(host) && rule.getTargetPort().equals(port)) {
                rule.getIsBreaked().set(0);
            }
        }
    }

    /**
     * 熔断某个HOST:PORT组合
     *
     * @param host 主机
     * @param port 端口
     */
    public void breakHostPort(String host, Integer port) {
        if ("DIRECT".equals(mode)) {
            throw new IllegalArgumentException("当前模式为直接模式，无法熔断");
        }
        for (HttpRouteRule rule : routeRules) {
            if (rule.getTargetHost().equals(host) && rule.getTargetPort().equals(port)) {
                rule.getIsBreaked().set(1);
            }
        }
    }

    /**
     * 获取路由规则状态
     *
     * @return 路由规则状态
     */
    public List<HttpRouteRule> getRouteRules() {
        return routeRules;
    }
}
