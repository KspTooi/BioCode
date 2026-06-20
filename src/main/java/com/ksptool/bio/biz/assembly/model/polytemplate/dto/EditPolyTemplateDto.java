package com.ksptool.bio.biz.assembly.model.polytemplate.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EditPolyTemplateDto {


    @Schema(description = "模板名称")
    private String name;

    @Schema(description = "模板代码")
    private String code;

    @Schema(description = "排序")
    private Integer seq;

    @Schema(description = "状态 0:禁用 1:启用")
    private Integer status;

}
