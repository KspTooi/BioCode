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

    @Schema(description = "业务表单名")
    private String bizFormName;

    @Schema(description = "发起人名")
    private String initiatorName;

    @Schema(description = "摘要(如：张三提交的 5000 元报销)")
    private String summary;

    @Schema(description = "待办状态 0:待办 1:已办")
    private Integer status;

    @Schema(description = "任务到达时间")
    private LocalDateTime createTime;

}
