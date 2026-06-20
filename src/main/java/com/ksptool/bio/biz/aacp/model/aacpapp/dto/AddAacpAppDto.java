package com.ksptool.bio.biz.aacp.model.aacpapp.dto;

import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import io.swagger.v3.oas.annotations.media.Schema;

@Getter
@Setter
public class AddAacpAppDto {

    @Schema(description="应用名称")
    private String name;

    @Schema(description="应用代码")
    private String code;

    @Schema(description="是否公开 0:不公开 1:公开")
    private Integer isPublic;

    @Schema(description="IP白名单列表")
    private String ips;

    @Schema(description="备注")
    private String remark;

    @Schema(description="状态 0:禁用 1:启用")
    private Integer status;

}
