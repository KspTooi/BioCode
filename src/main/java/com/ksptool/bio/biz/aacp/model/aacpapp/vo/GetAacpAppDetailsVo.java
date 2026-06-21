package com.ksptool.bio.biz.aacp.model.aacpapp.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Set;

@Getter
@Setter
public class GetAacpAppDetailsVo {

    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "应用名称")
    private String name;

    @Schema(description = "应用代码")
    private String code;

    @Schema(description = "访问密钥")
    private String appKey;

    @Schema(description = "是否公开 0:不公开 1:公开")
    private Integer isPublic;

    @Schema(description = "IP白名单列表")
    private Set<String> ips;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "状态 0:禁用 1:启用")
    private Integer status;

    @Schema(description = "绑定的模型变体ID列表")
    private List<Long> modelIds;
}
