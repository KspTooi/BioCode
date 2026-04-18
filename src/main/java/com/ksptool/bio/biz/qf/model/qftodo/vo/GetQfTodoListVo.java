package com.ksptool.bio.biz.qf.model.qftodo.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class GetQfTodoListVo {

    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "当前节点名称 (如: 财务总监审批)")
    private String nodeName;

    @Schema(description = "摘要(如：张三提交的 5000 元报销)")
    private String summary;

    @Schema(description = "办理成员类型 0:办理人, 1:候选组")
    private Integer memberType;

    @Schema(description = "办理成员ID (用户ID或用户组标识)")
    private Long memberId;

    @Schema(description = "发起人ID")
    private Long initiatorId;

    @Schema(description = "发起人名")
    private String initiatorName;

    @Schema(description = "发起时间")
    private LocalDateTime initiatorTime;

    @Schema(description = "任务到达时间")
    private LocalDateTime createTime;

}
