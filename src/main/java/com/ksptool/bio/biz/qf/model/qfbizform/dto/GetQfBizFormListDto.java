package com.ksptool.bio.biz.qf.model.qfbizform.dto;

import com.ksptool.assembly.entity.web.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GetQfBizFormListDto extends PageQuery {

    @Schema(description = "业务名称")
    private String name;

    @Schema(description = "业务编码")
    private String code;

    @Schema(description = "物理表名")
    private String tableName;

    @Schema(description = "状态 0:正常 1:停用")
    private Integer status;

    @Schema(description = "排序")
    private Integer seq;

}
