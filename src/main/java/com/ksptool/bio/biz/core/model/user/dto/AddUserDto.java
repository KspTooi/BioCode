package com.ksptool.bio.biz.core.model.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.Range;

import java.util.List;

@Getter
@Setter
public class AddUserDto {

    @Schema(description = "用户名")
    @NotBlank(message = "用户名不能为空")
    @Pattern(regexp = "^[a-zA-Z0-9_]{4,20}$", message = "用户名只能包含4-20位字母、数字和下划线")
    private String username;

    @Schema(description = "用户密码")
    @NotBlank(message = "用户密码不能为空")
    @Length(max = 128, message = "用户密码长度不能超过128个字符")
    private String password;

    @Length(max = 50, message = "用户昵称长度不能超过50个字符")
    @Schema(description = "用户昵称")
    private String nickname;

    @NotNull(message = "性别不能为空")
    @Range(min = 0, max = 2, message = "性别只能在0、1或2之间")
    @Schema(description = "性别 0:男 1:女 2:不愿透露")
    private Integer gender;

    @Length(max = 64, message = "用户手机号长度不能超过64个字符")
    @Schema(description = "用户手机号")
    private String phone;

    @Length(max = 64, message = "用户邮箱长度不能超过64个字符")
    @Schema(description = "用户邮箱")
    @Email(message = "邮箱格式不正确")
    private String email;

    @Range(min = 0, max = 1, message = "用户状态只能在0或1之间")
    @Schema(description = "用户状态 0:封禁 1:正常")
    private Integer status;

    @Schema(description = "所属组织架构ID 允许为公司、子公司、部门")
    private Long orgId;

    @NotNull(message = "用户组ID列表不能为空")
    @Schema(description = "用户组ID列表")
    private List<Long> groupIds;
}
