package com.ksptool.bio.biz.qf.model.qftodo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 取消待办事项DTO
 *
 * @author KspTool(ksptool@outlook.com)
 * @license Proprietary
 * 版权所有 (c) 2026 KspTool及其贡献者保留所有权利。
 * 未经事先书面许可，严禁任何形式的复制或分发。
 * @since 2026-05-09
 */
@Data
public class CancelQfTodoDto {

    @Schema(description = "待办ID")
    @NotNull(message = "待办ID不能为空")
    private Long id;

    @Schema(description = "取消原因")
    @NotBlank(message = "取消原因不能为空")
    @Size(max = 200, message = "取消原因长度不能超过200")
    private String reason;
}