package com.ksptool.bio.biz.aacp.model.aacpapp.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Set;

import org.hibernate.validator.constraints.Range;

@Getter
@Setter
public class AddAacpAppDto {

    @NotBlank(message = "应用名称不能为空")
    @Size(max = 40, message = "应用名称不能超过40个字符")
    @Schema(description = "应用名称")
    private String name;

    @NotBlank(message = "应用代码不能为空")
    @Size(max = 16, message = "应用代码不能超过16个字符")
    @Schema(description = "应用代码")
    private String code;

    @NotNull(message = "是否公开不能为空")
    @Range(min = 0, max = 1, message = "是否公开值无效，0:不公开 1:公开")
    @Schema(description = "是否公开 0:不公开 1:公开")
    private Integer isPublic;

    @NotNull(message = "IP白名单列表不能为空")
    @Schema(description = "IP白名单列表")
    private Set<String> ips;

    @Size(max = 200, message = "备注不能超过200个字符")
    @Schema(description = "备注")
    private String remark;

    @NotNull(message = "状态不能为空")
    @Range(min = 0, max = 1, message = "状态值无效，0:禁用 1:启用")
    @Schema(description = "状态 0:禁用 1:启用")
    private Integer status;

    @Schema(description = "绑定的模型变体ID列表")
    private List<Long> modelIds;
}