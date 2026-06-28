package com.ksptool.bio.biz.core.model.attachpool.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Range;

@Getter
@Setter
public class ScanAttachPoolDto {

    @NotNull(message = "扫描模式不能为空")
    @Range(min = 0, max = 1, message = "扫描模式只能为0(快速扫描)或1(深度扫描)")
    @Schema(description = "扫描模式 0:快速扫描 1:深度扫描")
    private Integer scanMode;

}
