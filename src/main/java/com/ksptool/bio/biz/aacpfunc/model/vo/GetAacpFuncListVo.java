package com.ksptool.bio.biz.aacpfunc.model.vo;

import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import io.swagger.v3.oas.annotations.media.Schema;

@Getter
@Setter
public class GetAacpFuncListVo {

    @Schema(description="主键ID")
    private Long id;

    @Schema(description="微函数名称")
    private String name;

    @Schema(description="微函数标识")
    private String code;

    @Schema(description="意图词")
    private String description;

}
