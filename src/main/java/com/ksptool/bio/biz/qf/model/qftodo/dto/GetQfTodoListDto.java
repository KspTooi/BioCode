package com.ksptool.bio.biz.qf.model.qftodo.dto;

import com.ksptool.assembly.entity.web.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class GetQfTodoListDto extends PageQuery {

    @Schema(description = "摘要(如：张三提交的 5000 元报销)")
    private String summary;

    @Schema(description = "办理成员类型 0:办理人, 1:候选组")
    private Integer memberType;

    @Schema(description = "办理成员ID (用户ID或用户组标识)")
    private Long memberId;

    @Schema(description = "发起人ID")
    private Long initiatorId;

    @Schema(description = "任务到达时间")
    private LocalDateTime createTime;

}
