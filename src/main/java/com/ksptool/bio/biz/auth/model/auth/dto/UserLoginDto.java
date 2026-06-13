package com.ksptool.bio.biz.auth.model.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserLoginDto {

    @NotBlank(message = "用户名不能为空")
    @Size(max = 1000, message = "用户名长度不能超过1000")
    @Schema(description = "用户名")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Size(max = 1000, message = "密码长度不能超过1000")
    @Schema(description = "密码")
    private String password;

}
