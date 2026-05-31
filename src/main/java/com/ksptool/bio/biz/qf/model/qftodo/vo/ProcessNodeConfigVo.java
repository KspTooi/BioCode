package com.ksptool.bio.biz.qf.model.qftodo.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 流程节点配置VO（发起流程时使用）
 */
@Data
public class ProcessNodeConfigVo {

    @Schema(description = "节点ID")
    private String nodeId;

    @Schema(description = "节点名称")
    private String nodeName;

    @Schema(description = "审批节点类型 0:固定人 1:发起时选人")
    private Integer aprKind;

    @Schema(description = "办理成员类型 0:用户 1:组 2:部门 10:任意人")
    private Integer memberKind;

    @Schema(description = "成员ID列表")
    private List<String> memberIds;

    @Schema(description = "成员名称列表")
    private List<String> memberNames;

}