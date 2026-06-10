package com.ksptool.bio.biz.aacp.model.mcp.dto;

import com.ksptool.bio.biz.core.common.aop.DtoCustomValidator;
import com.ksptool.bio.commons.dataprocess.Str;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.Range;

import java.util.List;

@Getter
@Setter
public class EditAacpMcpDto implements DtoCustomValidator {

    @NotNull(message = "主键ID不能为空")
    @Schema(description = "主键ID")
    private Long id;

    @NotBlank(message = "服务器名称不能为空")
    @Length(max = 40, message = "服务器名称长度不能超过40")
    @Schema(description = "服务器名称")
    private String name;

    @NotBlank(message = "唯一编码不能为空")
    @Length(max = 16, message = "唯一编码长度不能超过16")
    @Schema(description = "唯一编码")
    private String code;

    @NotNull(message = "通信协议不能为空")
    @Range(min = 0, max = 1, message = "通信协议只能在0-1之间")
    @Schema(description = "通信协议 0:HTTP+SSE 1:WS")
    private Integer networkKind;

    @NotNull(message = "鉴权类型不能为空")
    @Range(min = 0, max = 1, message = "鉴权类型只能在0-1之间")
    @Schema(description = "鉴权类型 0:无 1:PSK")
    private Integer authKind;

    @Length(max = 2000, message = "预共享密钥长度不能超过2000")
    @Schema(description = "预共享密钥")
    private String authPsk;

    @NotNull(message = "状态不能为空")
    @Range(min = 0, max = 1, message = "状态只能在0-1之间")
    @Schema(description = "状态 0:离线 1:在线")
    private Integer status;

    @NotNull(message = "能力包ID列表不能为空")
    @Schema(description = "能力包ID列表")
    private List<Long> capabilityIds;

    /**
     * 校验PSK鉴权时预共享密钥必填
     *
     * @return 错误信息 无错误返回null
     */
    @Override
    public String validate() {
        if (authKind != null && authKind == 1 && Str.isBlank(authPsk)) {
            return "使用PSK鉴权时必须填写预共享密钥";
        }
        if (capabilityIds.size() > 50) {
            return "一台MCP服务器最多绑定50个能力包";
        }
        return null;
    }

}
