package com.ksptool.bio.biz.aacp.model.capability.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class GetAacpCapabilityDetailsVo {

    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "能力包名称")
    private String name;

    @Schema(description = "类型 0:微函数")
    private Integer kind;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "关联的微函数ID列表")
    private List<Long> funcIds;

}
