package com.ksptool.bio.biz.auth.model.auth.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GetLoginConfigVo {

    @Schema(description = "登录验证码启用 0:关闭 1:开启")
    private Integer captchaEnabledLogin;

    @Schema(description = "是否允许弱密码 0:不允许 1:允许")
    private Integer aspAllowWeakPassword;

    @Schema(description = "是否允许密码包含用户名 0:不允许 1:允许")
    private Integer aspAllowUsernameInPassword;

    @Schema(description = "是否要求特殊字符 0:不要求 1:要求")
    private Integer aspRequireSpecial;

    @Schema(description = "密码最小长度")
    private Integer aspMinLength;

    @Schema(description = "是否允许记住密码 0:不允许 1:允许")
    private Integer enabledSavePasswordOnClient;

}