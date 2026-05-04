package com.ksptool.bio.biz.core.common.aop;

import com.google.gson.Gson;
import com.ksptool.assembly.entity.web.Result;
import com.ksptool.bio.biz.auth.service.AuthService;
import com.ksptool.bio.biz.core.common.AppRegistry;
import com.ksptool.bio.biz.core.service.RegistrySdk;
import com.ksptool.bio.commons.web.ResultCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * @author KspTooi
 * @since 1.6.14(N).45
 */
@Component
public class AppInstallWizardInterceptor implements HandlerInterceptor {

    @Autowired
    private RegistrySdk reg;

    @Autowired
    private Gson gson;

    @Override
    public boolean preHandle(@NonNull HttpServletRequest hsr, @NonNull HttpServletResponse hsrp, @NonNull Object handler) throws Exception {

        //用户没有登录，直接放行
        if (SecurityContextHolder.getContext().getAuthentication() == null) {
            return true;
        }

        //当前不处于安装向导模式,则直接放行
        if (reg.getInt(AppRegistry.CIW_ENABLED.getFullKey(), 0) == 0) {
            return true;
        }

        //当前在安装向导模式，先检查用户是否有超级权限
        if (AuthService.hasSuperCode()) {

            //超级用户有权在安装向导模式访问所有维护中心接口,这里直接放行
            var uri = hsr.getRequestURI();

            //判断是否是维护中心接口
            if (uri.startsWith("/maintain/") || uri.startsWith("/api/maintain/")) {
                return true;
            }

            //如果超级用户已登录,且访问的不是维护中心接口,需要引导他们到维护中心(通过返回102业务码,这样前端才能知道当前处于安装向导模式)
            var result = Result.error(ResultCode.INSTALL_WIZARD_ACTIVE.getCode(), "系统处于维护模式,请访问维护中心进行操作。");
            hsrp.setStatus(ResultCode.INSTALL_WIZARD_ACTIVE.getHttpStatus().value());
            hsrp.setContentType("application/json;charset=UTF-8");
            hsrp.setCharacterEncoding("UTF-8");
            hsrp.getWriter().write(gson.toJson(result));
            hsrp.flushBuffer();
            return false;
        }

        //普通用户,直接放行
        return true;
    }

}
