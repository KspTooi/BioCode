package com.ksptool.bio.biz.auth.controller;

import cloud.tianai.captcha.application.ImageCaptchaApplication;
import cloud.tianai.captcha.application.vo.ImageCaptchaVO;
import cloud.tianai.captcha.common.constant.CaptchaTypeConstant;
import cloud.tianai.captcha.common.response.ApiResponse;
import cloud.tianai.captcha.validator.common.model.dto.ImageCaptchaTrack;
import com.ksptool.assembly.entity.exception.AuthException;
import com.ksptool.assembly.entity.exception.BizException;
import com.ksptool.assembly.entity.web.Result;
import com.ksptool.bio.BioRunner;
import com.ksptool.bio.biz.auth.common.exception.AuthUnavailableException;
import com.ksptool.bio.biz.auth.common.exception.RootUnavailableException;
import com.ksptool.bio.biz.auth.model.auth.AuthUserSession;
import com.ksptool.bio.biz.auth.model.auth.dto.UserLoginDto;
import com.ksptool.bio.biz.auth.model.auth.vo.UserLoginVo;
import com.ksptool.bio.biz.auth.model.session.UserSessionPo;
import com.ksptool.bio.biz.auth.model.session.vo.UserSessionVo;
import com.ksptool.bio.biz.auth.service.SessionService;
import com.ksptool.bio.biz.core.common.AppRegistry;
import com.ksptool.bio.biz.core.model.user.dto.RegisterDto;
import com.ksptool.bio.biz.core.service.MenuService;
import com.ksptool.bio.biz.core.service.RegistrySdk;
import com.ksptool.bio.biz.core.service.UserService;
import com.ksptool.bio.commons.WebUtils;
import com.ksptool.bio.commons.annotation.PrintLog;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.event.AuthenticationFailureServiceExceptionEvent;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.Set;

import static com.ksptool.entities.Entities.as;

@PrintLog
@RestController
@Tag(name = "AUTH-认证管理", description = "认证管理")
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private SessionService sessionService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private ImageCaptchaApplication application;

    @Autowired
    private RegistrySdk regSdk;

    @Autowired
    private ApplicationEventPublisher aep;

    @Autowired
    private MenuService mService;

    @Operation(summary = "登录(新)")
    @PrintLog(sensitiveFields = "password")
    @PostMapping(value = "/userLogin")
    public Result<UserLoginVo> userLogin(@RequestBody UserLoginDto dto, HttpServletResponse hsrp) throws BizException {

        Authentication auth = null;

        try {
            // 使用Spring Security进行用户名密码认证
            auth = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(dto.getUsername(), dto.getPassword()));

        } catch (AuthenticationException e) {

            //如果异常是租户不可用异常，则返回租户不可用异常信息
            if (e.getCause() instanceof RootUnavailableException) {
                //发布登录失败事件(用于记录登录审计日志)
                aep.publishEvent(new AuthenticationFailureServiceExceptionEvent(new UsernamePasswordAuthenticationToken(dto.getUsername(), dto.getPassword()), e));
                return Result.error(e.getMessage());
            }

            //如果异常是用户被禁用异常 返回具体异常信息
            if (e.getCause() instanceof DisabledException) {
                aep.publishEvent(new AuthenticationFailureServiceExceptionEvent(new UsernamePasswordAuthenticationToken(dto.getUsername(), dto.getPassword()), e));
                return Result.error(e.getMessage());
            }

            //如果异常是认证不可用异常，则返回认证不可用异常信息
            if (e.getCause() instanceof AuthUnavailableException) {
                aep.publishEvent(new AuthenticationFailureServiceExceptionEvent(new UsernamePasswordAuthenticationToken(dto.getUsername(), dto.getPassword()), e));
                return Result.error(e.getMessage());
            }

            return Result.error("用户名或密码错误");
        }

        // 获取认证用户
        var aud = (AuthUserSession) auth.getPrincipal();

        if (aud == null) {
            return Result.error("获取当前登录主体失败!");
        }

        // 创建用户会话
        var sessionId = sessionService.createSession(aud);

        // 如果注册表中配置了允许使用Cookie鉴权, 下发Cookie到客户端
        if (regSdk.getInt(AppRegistry.FA_COOKIE_ALLOWED.getFullKey(), 0) == 1) {
            var cookieName = regSdk.getString(AppRegistry.FA_COOKIE_NAME.getFullKey(), "bio-session-id");
            var cookie = new Cookie(cookieName, sessionId);
            cookie.setPath("/");
            cookie.setHttpOnly(true); // 防止 XSS 攻击
            cookie.setMaxAge(7 * 24 * 60 * 60); // 7天有效期
            cookie.setAttribute("SameSite", "Lax");
            hsrp.addCookie(cookie);
        }

        // 组装Vo(如果Cookie鉴权被禁用, 只通过JSON返回给前端，前端需要自己在代码里面手动放Authorization: Bearer
        // <sessionId>请求头)
        var vo = as(aud, UserLoginVo.class);
        vo.setSessionId(sessionId);

        var version = BioRunner.getVersion();
        vo.setAppVersion(version.toString());
        vo.setAppVersionNumeric(version.toNumericVersion());

        //清除旧的用户菜单缓存
        mService.clearUserMenuTreeCacheByUserId(aud.getUserId());

        return Result.success(vo);
    }

    @Operation(summary = "注册")
    @PrintLog(sensitiveFields = "password")
    @PostMapping(value = "/register")
    @ResponseBody
    public Result<String> register(@Valid @RequestBody RegisterDto dto) {

        //String allowRegister = globalConfigService.getValue(GlobalConfigEnum.ALLOW_USER_REGISTER.getKey());

        //if (StringUtils.isBlank(allowRegister) || allowRegister.equals("false")) {
        //    return Result.error("管理员已禁用注册");
        //}

        try {
            var register = userService.register(dto.getUsername(), dto.getPassword());
            return Result.success("注册成功:" + register.getUsername());
        } catch (BizException e) {
            return Result.error(e);
        }

    }

    @Operation(summary = "用户注销")
    @PostMapping("/logout")
    public Result<String> logout(HttpServletRequest request) throws AuthException, BizException {
        String sessionId = WebUtils.getAuthenticationBearerSessionId(request);
        if (sessionId == null) {
            return Result.error("未登录");
        }
        UserSessionPo userSessionPo = sessionService.getSessionBySessionId(sessionId);
        if (userSessionPo == null) {
            return Result.error("未登录");
        }
        sessionService.closeSessionByPrimaryKey(userSessionPo.getId());
        return Result.success("注销成功");
    }

    @Operation(summary = "获取权限")
    @PostMapping("/getPermissions")
    @ResponseBody
    public Result<Set<String>> getPermissions(UserSessionVo session) {
        return Result.success(session.getPermissionCodes());
    }

    /**
     * 生成验证码
     *
     * @return 验证码数据
     */
    @PostMapping("/genCaptcha")
    public ApiResponse<ImageCaptchaVO> genCaptcha() {
        // 1.生成验证码(该数据返回给前端用于展示验证码数据)
        // 参数1为具体的验证码类型， 默认支持 SLIDER、ROTATE、WORD_IMAGE_CLICK、CONCAT 等验证码类型，详见：
        // `CaptchaTypeConstant`类
        return application.generateCaptcha(CaptchaTypeConstant.SLIDER);
    }

    /**
     * 校验验证码
     *
     * @param data 验证码数据
     * @return 校验结果
     */
    @PostMapping("/check")
    @ResponseBody
    public ApiResponse<?> checkCaptcha(@RequestBody Data data) {
        ApiResponse<?> response = application.matching(data.getId(), data.getData());
        if (response.isSuccess()) {
            return ApiResponse.ofSuccess(Collections.singletonMap("id", data.getId()));
        }
        return response;
    }

    @lombok.Data
    public static class Data {
        // 验证码id,前端回传的验证码ID
        private String id;
        // 验证码数据,前端回传的验证码轨迹数据
        private ImageCaptchaTrack data;
    }

}
