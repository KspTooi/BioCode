package com.ksptool.bio.biz.assembly.model.polytemplatefield.dto;

import com.ksptool.assembly.entity.web.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
public class GetPolyTemplateFieldListDto extends PageQuery {

    @Schema(description = "聚合模板ID")
    private Long polyTemplateId;

    @Schema(description = "字段名")
    private String name;

    @Schema(description = "可见性策略 ADD、EDIT、LIST_QUERY、LIST_VIEW")
    private Set<String> policyCrudJson;

    @Schema(description = "查询策略 0:等于")
    private Integer policyQuery;

    @Schema(description = "显示策略 0:文本框 1:文本域 2:下拉 3:单 4:多 5:LD 6:LDT")
    private Integer policyView;

    @Schema(description = "排序")
    private Integer seq;

}
