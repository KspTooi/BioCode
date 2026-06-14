package com.ksptool.bio.biz.aacp.model.datasource.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * 数据源信息 Vo（供微函数 datasource.list_databases 返回）
 */
@Getter
@Setter
public class DatasourceInfoVo {

    @Schema(description = "数据源ID")
    private Long id;

    @Schema(description = "数据源名称")
    private String name;

    @Schema(description = "数据源类型 0:MYSQL")
    private Integer kind;

    @Schema(description = "默认数据库")
    private String defaultDb;
}