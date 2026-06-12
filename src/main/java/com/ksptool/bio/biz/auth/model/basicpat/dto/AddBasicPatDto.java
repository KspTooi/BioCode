package com.ksptool.bio.biz.auth.model.basicpat.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class AddBasicPatDto {

    @NotNull(message = "PAT名称不能为空")
    @Size(max = 40, message = "PAT名称长度不能超过40")
    @Schema(description = "PAT名称")
    private String name;

    @Schema(description = "过期时间")
    private LocalDateTime expire;

}
