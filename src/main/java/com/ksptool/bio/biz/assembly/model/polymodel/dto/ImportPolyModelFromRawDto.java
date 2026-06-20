package com.ksptool.bio.biz.assembly.model.polymodel.dto;

import lombok.Getter;
import lombok.Setter;
import jakarta.validation.constraints.NotNull;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * @author KspTooi
 * @since 1.7.9(I).1
 */
@Getter
@Setter
public class ImportPolyModelFromRawDto {

    @NotNull(message = "输出方案ID不能为空")
    @Schema(description = "输出方案ID")
    private Long outputSchemaId;

    @Schema(description = "聚合模板ID(可选)")
    private Long polyTemplateId;

}
