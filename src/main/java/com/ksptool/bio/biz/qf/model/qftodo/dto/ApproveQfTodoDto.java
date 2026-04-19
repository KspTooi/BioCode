package com.ksptool.bio.biz.qf.model.qftodo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.Range;

/**
 * 审批待办事项DTO
 * 
 * @author KspTool(ksptool@outlook.com)
 * @since 2026-04-16
 */
@Getter
@Setter
public class ApproveQfTodoDto {

    @Schema(description = "主键ID")
    @NotNull(message = "主键ID不能为空")
    private Long id;

    @NotNull(message = "操作不能为空")
    @Range(min = 0, max = 1,message = "操作只能为0或1")
    @Schema(description = "操作 0:同意 1:驳回")
    private Integer action;

    @Schema(description = "审批意见")
    private String comment;

}
