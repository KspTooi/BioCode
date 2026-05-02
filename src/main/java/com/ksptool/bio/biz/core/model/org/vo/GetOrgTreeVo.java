package com.ksptool.bio.biz.core.model.org.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class GetOrgTreeVo {

    @Schema(description = "主键id")
    private Long id;

    @Schema(description = "顶级组织ID")
    private Long topId;

    @Schema(description = "上级组织ID NULL顶级组织")
    private Long parentId;

    @Schema(description = "0:企业 1:子企业 2:部门")
    private Integer kind;

    @Schema(description = "组织机构名称")
    private String name;

    @Schema(description = "级别")
    private Integer level;

    @Schema(description = "排序")
    private Integer seq;

    @Schema(description = "子组织")
    private List<GetOrgTreeVo> children;
}

