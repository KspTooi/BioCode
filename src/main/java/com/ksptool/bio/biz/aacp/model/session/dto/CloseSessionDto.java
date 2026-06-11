package com.ksptool.bio.biz.aacp.model.session.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CloseSessionDto {

    @NotBlank(message = "会话ID不能为空")
    @Schema(description = "会话ID")
    private String sessionId;
}
