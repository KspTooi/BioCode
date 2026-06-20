package com.ksptool.bio.biz.assembly.model.polymodel.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/**
 * @author KspTooi
 * @since 1.6.19(S).32
 */
@Getter
@Setter
public class GetPolyModelListDto {

    @NotNull(message = "输出方案ID不能为空")
    @Schema(description = "输出方案ID")
    private Long outputSchemaId;

}

