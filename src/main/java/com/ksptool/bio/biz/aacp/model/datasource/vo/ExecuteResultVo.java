package com.ksptool.bio.biz.aacp.model.datasource.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

/**
 * SQL 执行结果 Vo（供微函数 datasource.execute_query 返回）
 */
@Getter
@Setter
public class ExecuteResultVo {

    @Schema(description = "是否出错")
    private boolean error;

    @Schema(description = "错误或提示信息")
    private String message;

    @Schema(description = "执行的 SQL")
    private String sql;

    @Schema(description = "列名列表（仅 SELECT）")
    private List<String> columns;

    @Schema(description = "数据行（仅 SELECT）")
    private List<Map<String, Object>> rows;

    @Schema(description = "返回行数")
    private int rowCount;

    @Schema(description = "结果是否被截断")
    private boolean truncated;

    @Schema(description = "影响行数（仅 UPDATE/INSERT/DELETE）")
    private int affectedRows;
}