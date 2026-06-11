package com.ksptool.bio.biz.aacp.model.session.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class CloseSessionDto {

    @NotEmpty(message = "会话ID列表不能为空")
    @Schema(description = "会话ID列表")
    private List<String> sessionIds;
}
