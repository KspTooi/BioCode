package com.ksptool.bio.biz.assembly.model.polymodel.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

/**
 * @author KspTooi
 * @since 1.7.9(I).1
 */
@Getter
@Setter
public class GetPolyModelDetailsVo {

    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "输出方案ID")
    private Long outputSchemaId;

    @Schema(description = "字段名")
    private String name;

    @Schema(description = "数据类型")
    private String dataType;

    @Schema(description = "长度")
    private Integer length;

    @Schema(description = "必填 0:否 1:是")
    private Integer require;

    @Schema(description = "可见性策略 ADD、EDIT、LIST_QUERY、LIST_VIEW")
    private Set<String> policyCrudJson;

    @Schema(description = "查询策略 0:等于")
    private Integer policyQuery;

    @Schema(description = "显示策略 0:文本框 1:文本域 2:下拉 3:单 4:多 5:LD 6:LDT")
    private Integer policyView;

    @Schema(description = "是否主键 0:否 1:是")
    private Integer pk;

    @Schema(description = "字段备注")
    private String remark;

    @Schema(description = "排序")
    private Integer seq;

}

