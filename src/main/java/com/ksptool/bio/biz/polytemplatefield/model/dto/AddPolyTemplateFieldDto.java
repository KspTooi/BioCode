package com.ksptool.bio.biz.polytemplatefield.model.dto;

import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import io.swagger.v3.oas.annotations.media.Schema;

@Getter
@Setter
public class AddPolyTemplateFieldDto {

    @Schema(description="聚合模板ID")
    private Long polyTemplateId;

    @Schema(description="字段名")
    private String name;

    @Schema(description="可见性策略 ADD、EDIT、LIST_QUERY、LIST_VIEW")
    private String policyCrudJson;

    @Schema(description="查询策略 0:等于")
    private Integer policyQuery;

    @Schema(description="显示策略 0:文本框 1:文本域 2:下拉 3:单 4:多 5:LD 6:LDT")
    private Integer policyView;

    @Schema(description="排序")
    private Integer seq;

}
