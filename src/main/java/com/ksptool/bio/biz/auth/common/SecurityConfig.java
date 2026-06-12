package com.ksptool.bio.biz.auth.common;

import com.ksptool.bio.biz.auth.common.aop.PatSessionAuthFilter;
import com.ksptool.bio.biz.auth.common.aop.UserSessionAuthFilter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.context.NullSecurityContextRepository;

/**
 * @author KspTool
 * @since 1.5.23(W).109
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@Slf4j
public class SecurityConfig {

    /**
     * USAF过滤器 这个过滤器会获取请求中的Token并重建安全上下文
     */
    @Autowired
    private UserSessionAuthFilter usaf;

    /**
     * PAT虚拟会话过滤器 在USAF之前拦截PAT请求并改写Header
     */
    @Autowired
    private PatSessionAuthFilter patSessionAuthFilter;

    /**
     * 认证失败和权限不足的处理(统一返回JSON格式)
     */
    @Autowired
    private JsonAuthEntryPoint jaep;

    /**
     * 动态全局白名单管理器 这个管理器会检查请求是否在白名单中
     */
    @Autowired
    private DynamicGlobalWhiteManager dgwm;

    /**
     * 配置Spring Security
     *
     * @param hs HttpSecurity
     * @return SecurityFilterChain 安全过滤链
     * @throws Exception 异常
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity hs) throws Exception {

        //关闭CSRF(现在前后端使用Authorization头进行鉴权,停用CSRF校验)
        hs.csrf(AbstractHttpConfigurer::disable);

        //关闭Session
        hs.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
        //不使用 HttpSession 持久化 SecurityContext（每次请求由 token -> USAF 重建认证上下文）
        hs.securityContext(securityContext -> securityContext.securityContextRepository(new NullSecurityContextRepository()));

        //禁用 SavedRequest（默认会把原始请求缓存到 session）
        hs.requestCache(AbstractHttpConfigurer::disable);

        //禁用所有隐式依赖 session 的特性
        hs.formLogin(AbstractHttpConfigurer::disable);
        hs.httpBasic(AbstractHttpConfigurer::disable);
        hs.rememberMe(AbstractHttpConfigurer::disable);
        hs.logout(AbstractHttpConfigurer::disable);

        //一些默认 header 写入可能依赖会话/重定向语义，这里统一禁用；如需 HSTS/CSP/FrameOptions 再单独开启
        hs.headers(HeadersConfigurer::disable);

        //配置接口权限规则 使用DGWM进行权限管理
        hs.authorizeHttpRequests(auth -> auth
                .anyRequest().access(dgwm)
        );

        //配置认证失败和权限不足的处理(统一返回JSON格式)
        hs.exceptionHandling(exception -> exception
                .authenticationEntryPoint(jaep) // 认证失败
                .accessDeniedHandler(jaep) // 权限不足
        );

        //PAT虚拟会话过滤器 在USAF之前拦截PAT请求并把PAT转换为标准的Bearer Token格式
        hs.addFilterBefore(patSessionAuthFilter, UsernamePasswordAuthenticationFilter.class);
        
        //USAF过滤器会解析并验证用户会话（依赖 sessionId -> core_user_session 重建认证上下文），确保每次请求都能获取到有效的用户会话
        hs.addFilterBefore(usaf, UsernamePasswordAuthenticationFilter.class);

        //注意 上面的过滤器注册顺序是固定的，只能这样写 不能调换顺序！ 因为多个过滤器都以同一个参照物加入，先加的会排在前面。

        return hs.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public FilterRegistrationBean<UserSessionAuthFilter> registrationUserSessionAuthFilter(UserSessionAuthFilter filter) {
        FilterRegistrationBean<UserSessionAuthFilter> registration = new FilterRegistrationBean<>(filter);
        // 关键所在：禁止 Spring Boot 将其自动注册到全局 Servlet 过滤器链中
        registration.setEnabled(false);
        return registration;
    }
}
