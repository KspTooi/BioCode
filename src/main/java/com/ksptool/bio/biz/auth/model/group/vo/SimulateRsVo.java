package com.ksptool.bio.biz.auth.model.group.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class SimulateRsVo {

    @Schema(description = "本次模拟使用的RS等级")
    private Integer rsLevel;

    @Schema(description = "模拟节点ID")
    private Long orgId;

    @Schema(description = "模拟节点的kind 0:企业 1:子企业 2:部门 3:班组")
    private Integer nodeKind;

    @Schema(description = "是否为全量模式(rsLevel=0时为true,前端高亮全部节点;其余情况由visibleOrgIds决定)")
    private Boolean allMode;

    @Schema(description = "该等级下可见的组织节点ID集合(空集表示无任何可见节点)")
    private List<Long> visibleOrgIds;

}
