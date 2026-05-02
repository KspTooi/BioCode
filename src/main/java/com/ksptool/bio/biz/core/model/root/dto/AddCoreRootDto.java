package com.ksptool.bio.biz.core.model.root.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

import org.hibernate.validator.constraints.Range;
import org.hibernate.validator.constraints.Length;

@Getter
@Setter
public class AddCoreRootDto {

    @NotBlank( message = "租户名称不能为空")
    @Length(max = 40, message = "租户名称长度不能超过40个字符")
    @Schema(description = "租户名称")
    private String name;

    @Schema(description = "到期时间")
    private LocalDateTime expireTime;

    @Length(max = 200, message = "备注长度不能超过200个字符")
    @Schema(description = "备注")
    private String remark;

    @NotNull(message = "状态不能为空")
    @Range(min = 0, max = 1, message = "状态只能在0-1之间")
    @Schema(description = "状态 0:正常 1:禁用")
    private Integer status;

    @Schema(description = "管理员账号")
    @NotBlank(message = "管理员账号不能为空")
    @Length(max = 80, message = "管理员账号长度不能超过80个字符")
    private String adminUsername;

    @Schema(description = "管理员密码")
    @NotBlank(message = "管理员密码不能为空")
    @Length(max = 128, message = "管理员密码长度不能超过128个字符")
    private String adminPassword;

}
