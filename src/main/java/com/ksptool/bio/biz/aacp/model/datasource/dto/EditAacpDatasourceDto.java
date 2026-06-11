package com.ksptool.bio.biz.aacp.model.datasource.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.Range;

@Getter
@Setter
public class EditAacpDatasourceDto {

    @NotNull(message = "主键ID不能为空")
    @Schema(description = "主键ID")
    private Long id;

    @NotBlank(message = "数据源名称不能为空")
    @Length(max = 40, message = "数据源名称长度不能超过40")
    @Schema(description = "数据源名称")
    private String name;

    @NotBlank(message = "数据源编码不能为空")
    @Length(max = 32, message = "数据源编码长度不能超过32")
    @Schema(description = "数据源编码")
    private String code;

    @NotNull(message = "数据源类型不能为空")
    @Range(min = 0, max = 0, message = "当前仅支持 MySQL 数据源")
    @Schema(description = "数据源类型 0:MYSQL")
    private Integer kind;

    @NotBlank(message = "JDBC驱动不能为空")
    @Length(max = 200, message = "JDBC驱动长度不能超过200")
    @Schema(description = "JDBC驱动")
    private String drive;

    @NotBlank(message = "连接字符串不能为空")
    @Schema(description = "连接字符串")
    private String url;

    @Length(max = 200, message = "连接用户名长度不能超过200")
    @Schema(description = "连接用户名")
    private String username;

    @Length(max = 2000, message = "连接密码长度不能超过2000")
    @Schema(description = "连接密码")
    private String password;

    @NotBlank(message = "默认数据库不能为空")
    @Length(max = 200, message = "默认数据库长度不能超过200")
    @Schema(description = "默认数据库")
    private String defaultDb;

    @NotNull(message = "最大查询行数不能为空")
    @Schema(description = "最大查询行数")
    private Integer queryMaxRows;

    @NotNull(message = "是否支持批处理不能为空")
    @Range(min = 0, max = 1, message = "批处理只能在0-1之间")
    @Schema(description = "是否支持批处理 0:不支持 1:支持")
    private Integer executeBatch;
}
