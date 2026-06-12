package com.ksptool.bio.biz.auth.common;

import com.ksptool.bio.biz.core.service.RegistrySdk;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ResourceLoader;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.authorization.AuthorizationResult;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * 动态全局白名单管理器
 * 运作原理
 * 请求进入 --> 身份认证(由USAF重建安全上下文) --> DGWM检查(不在白名单里面且没有安全上下文的请求全部拒绝) --> 放行
 *
 * @author KspTool
 * @since 1.6.14(N)1
 */
@Slf4j
@Component
public class DynamicGlobalWhiteManager implements AuthorizationManager<RequestAuthorizationContext> {

    //使用 Spring Security 7.0 全新推荐的 PathPatternRequestMatcher
    private final List<PathPatternRequestMatcher> whiteListMatchers = new ArrayList<>();

    @Autowired
    private ResourceLoader resourceLoader;

    @Autowired
    private RegistrySdk reg;

    //缓存是否是集成部署(通常情况下这个值不会变化,所以缓存起来)
    private Boolean isIntegratedDeploy;

    /**
     * 初始化白名单规则
     */
    @PostConstruct
    public void init() {

        var basic = new HashSet<String>();
        var integratedDeploy = new HashSet<String>();

        //构建基础白名单
        basic.add("/auth/userLogin"); //用户登录
        basic.add("/auth/getLoginConfig"); //获取登录配置
        basic.add("/auth/genCaptcha"); //验证码端点
        basic.add("/auth/check"); //验证码端点
        basic.add("/v3/api-docs"); //OpenApi 端点
        basic.add("/static/**");
        basic.add("/druid/**"); //Druid监控页面

        //构建集成部署白名单
        if (isIntegratedDeploy()) {
            integratedDeploy.add("/api/auth/userLogin"); //用户登录
            integratedDeploy.add("/auth/getLoginConfig"); //获取登录配置
            integratedDeploy.add("/api/auth/genCaptcha"); //验证码端点
            integratedDeploy.add("/api/auth/check"); //验证码端点
            integratedDeploy.add("/api/v3/api-docs"); //OpenApi 端点
            integratedDeploy.add("/index.html"); //主页
            integratedDeploy.add("/favicon.ico"); //网站图标
            integratedDeploy.add("/"); //根路径
            integratedDeploy.add("/login"); //登录页
            integratedDeploy.add("/js/**"); //静态资源
            integratedDeploy.add("/css/**"); //静态资源
            integratedDeploy.add("/assets/**"); //静态资源
            integratedDeploy.add("/static/**"); //静态资源

        }

        //将基础白名单和集成部署白名单转换为PPRM并添加到白名单列表中
        whiteListMatchers.addAll(basic.stream().map(PathPatternRequestMatcher::pathPattern).collect(Collectors.toSet()));
        whiteListMatchers.addAll(integratedDeploy.stream().map(PathPatternRequestMatcher::pathPattern).collect(Collectors.toSet()));

        if (isIntegratedDeploy()) {
            log.info("DGWM 白名单加载: 当前已启用集成部署模式，总条目: {}", whiteListMatchers.size());
        }

        if (!isIntegratedDeploy()) {
            log.info("DGWM 白名单加载: 当前处于标准部署模式，总条目: {}", whiteListMatchers.size());
        }

    }

    @Override
    public @Nullable AuthorizationResult authorize(@NonNull Supplier<? extends @Nullable Authentication> authentication, RequestAuthorizationContext rac) {

        //如果请求上下文为空，则直接拒绝
        if (rac == null) {
            return new AuthorizationDecision(false);
        }

        //获取请求对象
        HttpServletRequest request = rac.getRequest();

        //检查是否匹配白名单
        boolean isWhiteListed = whiteListMatchers.stream()
                .anyMatch(matcher -> matcher.matches(request));

        if (isWhiteListed) {
            // 白名单直接放行
            return new AuthorizationDecision(true);
        }

        //非白名单请求，执行兜底认证检查 (必须已登录且非匿名)
        Authentication auth = authentication.get();
        boolean isAuth = auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken);

        return new AuthorizationDecision(isAuth);
    }

    /**
     * 判断是否是集成部署
     */
    private boolean isIntegratedDeploy() {

        if (isIntegratedDeploy != null) {
            return isIntegratedDeploy;
        }

        //如果缓存为空,则从资源加载器中获取是否是集成部署
        isIntegratedDeploy = resourceLoader.getResource("classpath:/web-static/index.html").exists();
        return isIntegratedDeploy;
    }

    /**
     * 获取白名单匹配器
     * @return 白名单匹配器列表
     */
    public List<PathPatternRequestMatcher> getWhiteListMatchers() {
        return whiteListMatchers;
    }
}