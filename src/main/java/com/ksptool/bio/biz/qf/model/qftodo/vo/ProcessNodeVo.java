package com.ksptool.bio.biz.qf.model.qftodo.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 流程节点VO
 * 用于封装流程图中节点的基本信息和状态
 */
@Data
public class ProcessNodeVo {
    /**
     * 节点ID
     */
    @Schema(description = "节点ID")
    private String nodeId;

    /**
     * 节点名称
     */
    @Schema(description = "节点名称")
    private String nodeName;

    /**
     * 节点状态
     */
    @Schema(description = "节点状态")
    private String status;

    /**
     * 节点颜色（用于流程图展示）
     */
    @Schema(description = "节点颜色")
    private String color;
}
