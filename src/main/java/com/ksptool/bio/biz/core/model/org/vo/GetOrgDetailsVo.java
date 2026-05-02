package com.ksptool.bio.biz.core.model.org.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GetOrgDetailsVo {

    @Schema(description = "主键id")
    private Long id;

    @Schema(description = "顶级组织ID")
    private Long topId;

    @Schema(description = "直属企业ID")
    private Long orgId;

    @Schema(description = "上级组织ID NULL顶级组织")
    private Long parentId;

    @Schema(description = "0:企业 1:子企业 2:部门")
    private Integer kind;

    @Schema(description = "组织机构名称")
    private String name;

    @Schema(description = "组织机构简称")
    private String shortName;

    @Schema(description = "排序")
    private Integer seq;

    @Schema(description = "备注")
    private String remark;
}

