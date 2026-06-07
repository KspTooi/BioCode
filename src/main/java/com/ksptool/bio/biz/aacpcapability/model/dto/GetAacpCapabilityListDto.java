package com.ksptool.bio.biz.aacpcapability.model.dto;

import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import com.ksptool.assembly.entity.web.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GetAacpCapabilityListDto extends PageQuery {

    @Schema(description="能力包名称")
    private String name;

    @Schema(description="类型 0:微函数")
    private Integer kind;

}
