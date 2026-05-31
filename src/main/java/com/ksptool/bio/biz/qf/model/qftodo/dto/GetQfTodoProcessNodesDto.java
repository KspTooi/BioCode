package com.ksptool.bio.biz.qf.model.qftodo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GetQfTodoProcessNodesDto {
    @NotBlank(message = "引擎流程ID不能为空")
    @Schema(description = "引擎流程ID")
    private String engProcId;

    public GetQfTodoProcessNodesDto(String engProcId) {
        this.engProcId = engProcId;
    }
    public GetQfTodoProcessNodesDto() {
    }
}
