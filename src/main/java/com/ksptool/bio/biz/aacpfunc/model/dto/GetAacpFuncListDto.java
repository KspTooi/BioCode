package com.ksptool.bio.biz.aacpfunc.model.dto;

import com.ksptool.assembly.entity.web.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GetAacpFuncListDto extends PageQuery {

    @Schema(description = "微函数名称")
    private String name;

    @Schema(description = "微函数标识")
    private String code;

    @Schema(description = "意图词")
    private String description;

}
