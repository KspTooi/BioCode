package com.ksptool.bio.biz.aacp.model.provider.dto;

import com.ksptool.bio.biz.core.common.aop.DtoCustomValidator;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.validator.constraints.Range;

@Getter
@Setter
public class AddProviderDto implements DtoCustomValidator {

    @NotBlank(message = "供应商名称不能为空")
    @Size(max = 80, message = "供应商名称不能超过80个字符")
    @Schema(description = "供应商名称")
    private String name;

    @NotBlank(message = "供应商代码不能为空")
    @Size(max = 32, message = "供应商代码不能超过32个字符")
    @Schema(description = "供应商代码")
    private String code;

    @Size(max = 2000, message = "接口密钥不能超过2000个字符")
    @Schema(description = "接口密钥")
    private String apiKey;

    @NotNull(message = "接口类型不能为空")
    @Range(min = 0, max = 1, message = "接口类型值无效，0:OpenAi 1:Anthropic")
    @Schema(description = "接口类型 0:OpenAi 1:Anthropic")
    private Integer apiKind;

    @NotBlank(message = "接口地址不能为空")
    @Size(max = 512, message = "接口地址不能超过512个字符")
    @Schema(description = "接口地址")
    private String apiHost;

    @NotBlank(message = "接口端点不能为空")
    @Size(max = 512, message = "接口端点不能超过512个字符")
    @Schema(description = "接口端点")
    private String apiUrl;

    @NotNull(message = "代理类型不能为空")
    @Range(min = 0, max = 2, message = "代理类型值无效，0:无 1:HTTP 2:SOCKS5")
    @Schema(description = "代理类型 0:无 1:HTTP 2:SOCKS5")
    private Integer proxyKind;

    @Size(max = 512, message = "代理地址不能超过512个字符")
    @Schema(description = "代理地址")
    private String proxyUrl;

    @NotNull(message = "状态不能为空")
    @Range(min = 0, max = 1, message = "状态值无效，0:禁用 1:启用")
    @Schema(description = "状态 0:禁用 1:启用")
    private Integer status;

    @Override
    public String validate() {
        if (proxyKind > 0 && StringUtils.isBlank(proxyUrl)) {
            return "代理类型不为无时，代理地址不能为空";
        }
        return null;
    }
}