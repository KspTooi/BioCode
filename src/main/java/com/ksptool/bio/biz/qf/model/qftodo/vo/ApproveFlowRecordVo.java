package com.ksptool.bio.biz.qf.model.qftodo.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;


/**
 * 审批流程记录VO
 * 用于封装审批流程中各个节点的审批信息
 */
@Data
public class ApproveFlowRecordVo {
    /**
     * 节点名称
     */
    @Schema(description = "节点名称")
    private String nodeName;

    /**
     * 节点审批人
     */
    @Schema(description = "节点审批人")
    private String finMemberName;

    /**
     * 节点审批时间
     */
    @Schema(description = "节点审批时间")
    private LocalDateTime finTime;

    /**
     * 节点审批结果
     */
    @Schema(description = "节点审批结果")
    private Integer action;

    /**
     * 审批意见
     */
    @Schema(description = "审批意见")
    private String comment;

    /**
     * 待办状态
     */
    @Schema(description = "待办状态")
    private Integer status;
}
