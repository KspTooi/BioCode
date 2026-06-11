package com.ksptool.bio.biz.aacp.model.datasource.dto;

import com.ksptool.assembly.entity.web.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GetAacpDatasourceListDto extends PageQuery {

    @Schema(description = "数据源名称")
    private String name;

    @Schema(description = "数据源编码")
    private String code;
}
