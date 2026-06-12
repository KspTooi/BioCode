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
import com.ksptool.bio.biz.auth.common.ChaCha20Poly1305;
import com.ksptool.bio.biz.auth.common.exception.AuthUnavailableException;
import com.ksptool.bio.biz.auth.common.exception.RootUnavailableException;
import com.ksptool.bio.biz.auth.model.auth.AuthUserSession;
import com.ksptool.bio.biz.auth.model.auth.dto.PatLoginDto;
import com.ksptool.bio.biz.auth.model.auth.dto.UserLoginDto;
import com.ksptool.bio.biz.auth.model.auth.vo.UserLoginVo;
import com.ksptool.bio.biz.auth.model.basicpat.BasicPatPo;
import com.ksptool.bio.biz.auth.model.session.UserSessionPo;
import com.ksptool.bio.biz.auth.model.session.vo.UserSessionVo;
import com.ksptool.bio.biz.auth.service.AuthUserDetailsService;
import com.ksptool.bio.biz.auth.service.BasicPatService;
import com.ksptool.bio.biz.auth.service.SessionService;
import com.ksptool.bio.biz.core.common.AppRegistry;
import com.ksptool.bio.biz.core.model.user.dto.RegisterDto;
import com.ksptool.bio.biz.core.repository.UserRepository;
import com.ksptool.bio.biz.core.service.MenuService;
import com.ksptool.bio.biz.core.service.RegistrySdk;
import com.ksptool.bio.biz.core.service.UserService;
import com.ksptool.bio.commons.WebUtils;
import com.ksptool.bio.commons.annotation.PrintLog;
import com.ksptool.bio.commons.dataprocess.Str;

import ch.qos.logback.core.util.StringUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.event.AuthenticationFailureServiceExceptionEvent;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.*;

import com.ksptool.bio.biz.auth.model.auth.vo.GetLoginConfigVo;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.Base64;
import java.util.Collections;
import java.util.Set;

import static com.ksptool.entities.Entities.as;
import lombok.extern.slf4j.Slf4j;

@PrintLog
@RestController
@Tag(name = "AUTH-认证管理", description = "认证管理")
@RequestMapping("/auth")
@Slf4j
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

    @Autowired
    private BasicPatService basicPatService;

    @Autowired
    private AuthUserDetailsService authUserDetailsService;

    @Autowired
    private UserRepository userRepository;


    @Value("${auth.login.key}")
    private String authLoginKey;

    @Operation(summary = "登录(新)")
    @PrintLog(sensitiveFields = "password")
    @PostMapping(value = "/userLogin")
    public Result<UserLoginVo> userLogin(@RequestBody UserLoginDto dto, HttpServletResponse hsrp) throws BizException, GeneralSecurityException {

        Authentication auth = null;

        //预处理密文
        //CT 密文CipherText
        var unCtWithIv = dto.getUsername();
        var pwCtWithIv = dto.getPassword();

        //解析IV
        var unCtMix = Str.safeSplit(unCtWithIv, ":");
        var pwCtMix = Str.safeSplit(pwCtWithIv, ":");

        if(unCtMix.size() != 2 || pwCtMix.size() != 2){
            return Result.error("解析用户名或密码时发生错误！请检查数据格式。");
        }

        var unCt = unCtMix.get(0);
        var pwCt = pwCtMix.get(0);
        var unIv = unCtMix.get(1);
        var pwIv = pwCtMix.get(1);

        if(StringUtils.isBlank(unIv) || StringUtils.isBlank(pwIv)){
            return Result.error("获取初始化向量失败！");
        }

        if(unIv.equals(pwIv)){
            return Result.error("初始化向量相同！无法进行解密。");
        }

        if (StringUtils.isBlank(unCt) || StringUtils.isBlank(pwCt)) {
            return Result.error("获取原始账号密码失败！");
        }

        //已集齐要素、可以安全解密         
        //PT 明文PlainText
        var unPt = "";
        var pwPt = "";

        try {

            var unIvBytes = Base64.getDecoder().decode(unIv);
            var pwIvBytes = Base64.getDecoder().decode(pwIv);
            var pwCtBytes = Base64.getDecoder().decode(pwCt);
            var unCtBytes = Base64.getDecoder().decode(unCt);

            var psk = ChaCha20Poly1305.getSecretKey(Base64.getDecoder().decode(authLoginKey));
            var decryptedPw = ChaCha20Poly1305.decrypt(pwCtBytes, psk, pwIvBytes, null);
            var decryptedUn = ChaCha20Poly1305.decrypt(unCtBytes, psk, unIvBytes, null);
            pwPt = new String(decryptedPw, StandardCharsets.UTF_8);
            unPt = new String(decryptedUn, StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("解密用户名或密码时发生错误！", e);
            return Result.error("无法解析用户名或密码！");
        }

        if(StringUtils.isBlank(unPt) || StringUtils.isBlank(pwPt)){
            return Result.error("无法解析用户名或密码！");
        }

        try {

            // 使用Spring Security进行用户名密码认证
            auth = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(unPt, pwPt));

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

    /**
     * PAT令牌登录：前端用ChaCha20-Poly1305加密令牌后传输，后端解密验证
     */
    @Operation(summary = "PAT令牌登录")
    @PostMapping(value = "/patLogin")
    public Result<UserLoginVo> patLogin(@RequestBody @Valid PatLoginDto dto) throws BizException, GeneralSecurityException {

        //检查是否允许显式PAT登录(关闭后仅PSAF静默登录可用)
        if (regSdk.getInt(AppRegistry.FA_ALLOW_PAT_LOGIN.getFullKey(), 1) != 1) {
            return Result.error("管理员已禁用PAT显式登录");
        }

        //解析密文与IV
        var ctWithIv = dto.getPatToken();
        var ctMix = Str.safeSplit(ctWithIv, ":");

        if (ctMix.size() != 2) {
            return Result.error("解析PAT令牌时发生错误！请检查数据格式。");
        }

        var ct = ctMix.get(0);
        var iv = ctMix.get(1);

        if (StringUtils.isBlank(ct) || StringUtils.isBlank(iv)) {
            return Result.error("获取PAT令牌密文或初始化向量失败！");
        }

        //解密PAT令牌明文
        var patPt = "";
        try {
            var ivBytes = Base64.getDecoder().decode(iv);
            var ctBytes = Base64.getDecoder().decode(ct);
            var psk = ChaCha20Poly1305.getSecretKey(Base64.getDecoder().decode(authLoginKey));
            var decrypted = ChaCha20Poly1305.decrypt(ctBytes, psk, ivBytes, null);
            patPt = new String(decrypted, StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("解密PAT令牌时发生错误！", e);
            return Result.error("无法解析PAT令牌！");
        }

        if (StringUtils.isBlank(patPt)) {
            return Result.error("无法解析PAT令牌！");
        }

        //验证PAT令牌
        BasicPatPo patPo;
        try {
            patPo = basicPatService.validatePat(patPt);
        } catch (BizException e) {
            return Result.error(e.getMessage());
        }

        //根据PAT创建者查询用户名
        var userPo = userRepository.findById(patPo.getCreatorId())
                .orElse(null);
        if (userPo == null) {
            return Result.error("PAT令牌对应的用户不存在！");
        }

        //加载用户完整详情(权限、RS数据等)
        var aus = (AuthUserSession) authUserDetailsService.loadUserByUsername(userPo.getUsername());
        aus.setLoginType(1);

        //创建PAT虚拟会话
        sessionService.createPatSession(aus, patPt);

        //组装Vo
        var vo = as(aus, UserLoginVo.class);
        vo.setSessionId(patPt);

        var version = BioRunner.getVersion();
        vo.setAppVersion(version.toString());
        vo.setAppVersionNumeric(version.toNumericVersion());

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

    @Operation(summary = "获取登录配置")
    @PostMapping("/getLoginConfig")
    @ResponseBody
    public Result<GetLoginConfigVo> getLoginConfig() {
        var vo = new GetLoginConfigVo();
        vo.setCaptchaEnabledLogin(regSdk.getInt(AppRegistry.FA_CAPTCHA_ENABLED_LOGIN.getFullKey(), 0));
        vo.setAspAllowWeakPassword(regSdk.getInt(AppRegistry.FA_ASP_ALLOW_WEAK_PASSWORD.getFullKey(), 1));
        vo.setAspAllowUsernameInPassword(regSdk.getInt(AppRegistry.FA_ASP_ALLOW_USERNAME_IN_PASSWORD.getFullKey(), 1));
        vo.setAspRequireSpecial(regSdk.getInt(AppRegistry.FA_ASP_REQUIRE_SPECIAL.getFullKey(), 0));
        vo.setAspMinLength(regSdk.getInt(AppRegistry.FA_ASP_MIN_LENGTH.getFullKey(), 8));
        vo.setEnabledSavePasswordOnClient(regSdk.getInt(AppRegistry.FA_ENABLED_SAVE_PASSWORD_ON_CLIENT.getFullKey(), 0));
        vo.setAllowPatLogin(regSdk.getInt(AppRegistry.FA_ALLOW_PAT_LOGIN.getFullKey(), 1));
        return Result.success(vo);
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
