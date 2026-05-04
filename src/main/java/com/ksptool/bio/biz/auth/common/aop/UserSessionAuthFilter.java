package com.ksptool.bio.biz.auth.common.aop;

import com.ksptool.assembly.entity.exception.BizException;
import com.ksptool.bio.biz.auth.model.auth.AuthUserSession;
import com.ksptool.bio.biz.auth.model.session.UserSessionPo;
import com.ksptool.bio.biz.auth.service.SessionService;
import com.ksptool.bio.biz.core.common.AppRegistry;
import com.ksptool.bio.biz.core.service.RegistrySdk;
import com.ksptool.bio.commons.WebUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.NullMarked;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.HashSet;

import static com.ksptool.entities.Entities.as;

/**
 * 基于 core_user_session 的无状态认证过滤器模板。
 * <p>
 * 这个类主要用于基于 core_user_session 的无状态认证过滤器，确保每次请求都能获取到有效的用户会话。
 * 它会从请求头或Cookie中获取sessionId，然后从数据库查询会话信息，并重建认证上下文。
 * <p>
 * 认证流程
 * 1.用户登录，通过/auth/userLogin接口，返回sessionId 服务端会设置一个名为bio-session-id的Cookie
 * (前端也可以手动发送Authorization: Bearer <sessionId>请求头)
 * 2.用户每次请求，会自动携带bio-session-id Cookie或Authorization: Bearer <sessionId>请求头
 * 3.后端收到请求，通过这个过滤器，获取sessionId，并从数据库查询(此处已有缓存机制,不会每次都查询数据库)会话信息，如果会话存在且未过期，则重建安全上下文
 * 4.重建安全上下文后，SpringSecurity会自动将安全上下文设置到SecurityContextHolder中，后续的接口调用会自动使用这个安全上下文
 *
 * @author KspTool
 * @since 1.5.23(W).109
 */
@Component
public class UserSessionAuthFilter extends OncePerRequestFilter {

    @Autowired
    private SessionService sessionService;

    @Autowired
    private CacheManager cacheManager;

    @Autowired
    private RegistrySdk regSdk;

    @NullMarked
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // 如果已经认证，则直接放行
        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            filterChain.doFilter(request, response);
            return;
        }

        // 优先从Authentication头中获取sessionId
        var sessionId = WebUtils.getAuthenticationBearerSessionId(request);

        // 如果Authentication头中获取不到sessionId，则从Cookie中获取
        if (StringUtils.isBlank(sessionId)) {

            // 但在这之前，需要检查注册表中是否配置了允许使用Cookie鉴权, 如果配置了，才从Cookie中获取
            if (regSdk.getInt(AppRegistry.FA_COOKIE_ALLOWED.getFullKey(), 0) == 1) {
                var cookieName = regSdk.getString(AppRegistry.FA_COOKIE_NAME.getFullKey(), "bio-session-id");
                sessionId = WebUtils.getCookieValue(request, cookieName);
            }

        }

        // 如果从Cookie和Auth头都获取不到 则从请求参数中获取
        if (StringUtils.isBlank(sessionId)) {
            sessionId = request.getParameter("token");
        }

        // 如果sessionId为空，则直接放行(无请求上下文)
        if (StringUtils.isBlank(sessionId)) {
            filterChain.doFilter(request, response);
            return;
        }

        // 从数据库查询会话信息(getSessionBySessionId已配置缓存,不会每次都查询数据库)
        UserSessionPo sessionPo = null;

        try {
            sessionPo = sessionService.getSessionBySessionId(sessionId);

            // 如果系统使用了缓存,需要检查缓存中是否标记了该用户数据版本已变更,如果已变更,则刷新会话
            var cache = cacheManager.getCache("userSession");
            if (cache != null) {
                var key = "user_dv_changed_" + sessionPo.getUserId();

                if (cache.get(key) != null) {
                    sessionPo = sessionService.refreshSession(sessionPo);
                    cache.evict(key);
                }

            }

        } catch (BizException e) {
            filterChain.doFilter(request, response);
            return;
        }

        // 如果会话不存在，则直接放行(无请求上下文)
        if (sessionPo == null) {
            filterChain.doFilter(request, response);
            return;
        }

        // 如果会话已过期，则直接放行(无请求上下文)
        if (sessionPo.isExpired()) {
            filterChain.doFilter(request, response);
            return;
        }

        // 会话正常，配置安全上下文
        var authorities = new HashSet<GrantedAuthority>();

        // 解析会话中的权限码(存储在数据库中的权限码是JSON数组且角色已有ROLE_前缀)
        var permissionCodes = sessionPo.getPermissionCodes();

        for (var permissionCode : permissionCodes) {
            if (StringUtils.isBlank(permissionCode)) {
                continue;
            }
            authorities.add(new SimpleGrantedAuthority(permissionCode));
        }

        // UserSessionPo 转换为 AuthUserDetails
        var aud = as(sessionPo, AuthUserSession.class);
        aud.setUserId(sessionPo.getUserId());

        // 设置RS数据权限到AUD中
        aud.setRsMax(sessionPo.getRsMax());
        aud.setRsAllowOrgIds(sessionPo.getRsAllowOrgIds());
        var authentication = new UsernamePasswordAuthenticationToken(aud, null, authorities);

        // 设置认证详情
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        filterChain.doFilter(request, response);
    }
}
