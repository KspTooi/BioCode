package com.ksptool.bio.biz.aacp.model.datasource.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class GetAacpDatasourceDetailsVo {

    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "数据源名称")
    private String name;

    @Schema(description = "数据源编码")
    private String code;

    @Schema(description = "数据源类型 0:MYSQL")
    private Integer kind;

    @Schema(description = "JDBC驱动")
    private String drive;

    @Schema(description = "连接字符串")
    private String url;

    @Schema(description = "连接用户名")
    private String username;

    @Schema(description = "连接密码")
    private String password;

    @Schema(description = "默认数据库")
    private String defaultDb;

    @Schema(description = "最大查询行数")
    private Integer queryMaxRows;

    @Schema(description = "是否支持批处理 0:不支持 1:支持")
    private Integer executeBatch;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
