package com.ksptool.bio.biz.aacp.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.Range;

@Getter
@Setter
public class AddAacpMcpDto {

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

    @NotBlank(message = "主机不能为空")
    @Length(max = 45, message = "主机长度不能超过45")
    @Schema(description = "主机")
    private String host;

    @NotNull(message = "端口不能为空")
    @Range(min = 1, max = 65535, message = "端口只能在1-65535之间")
    @Schema(description = "端口")
    private Integer port;

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

}
