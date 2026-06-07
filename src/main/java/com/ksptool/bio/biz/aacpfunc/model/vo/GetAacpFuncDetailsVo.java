package com.ksptool.bio.biz.aacpfunc.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GetAacpFuncDetailsVo {

    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "微函数名称")
    private String name;

    @Schema(description = "微函数标识")
    private String code;

    @Schema(description = "意图词")
    private String description;

    @Schema(description = "入参规范")
    private String schema;

    @Schema(description = "调用目标Bean")
    private String target;

    @Schema(description = "备注")
    private String remark;

}
