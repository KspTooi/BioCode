package com.ksptool.bio.biz.aacpcapability.model.vo;

import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import io.swagger.v3.oas.annotations.media.Schema;

@Getter
@Setter
public class GetAacpCapabilityListVo {

    @Schema(description="主键ID")
    private Long id;

    @Schema(description="能力包名称")
    private String name;

    @Schema(description="类型 0:微函数")
    private Integer kind;

    @Schema(description="备注")
    private String remark;

}
