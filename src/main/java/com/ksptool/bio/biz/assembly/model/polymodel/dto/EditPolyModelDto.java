package com.ksptool.bio.biz.assembly.model.polymodel.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Range;

import java.util.Set;

/**
 * @author KspTooi
 * @since 1.6.19(S).32
 */
@Getter
@Setter
public class EditPolyModelDto {

    @NotNull(message = "主键ID不能为空")
    @Schema(description = "主键ID")
    private Long id;

    @NotNull(message = "输出方案ID不能为空")
    @Schema(description = "输出方案ID")
    private Long outputSchemaId;

    @NotBlank(message = "字段名不能为空")
    @Size(max = 255, message = "字段名长度不能超过255")
    @Schema(description = "字段名")
    private String name;

    @NotBlank(message = "数据类型不能为空")
    @Size(max = 255, message = "数据类型长度不能超过255")
    @Schema(description = "数据类型")
    private String dataType;

    @Schema(description = "长度")
    private Integer length;

    @NotNull(message = "必填不能为空")
    @Range(min = 0, max = 1, message = "必填值只能为0或1")
    @Schema(description = "必填 0:否 1:是")
    private Integer require;

    @NotNull(message = "可见性策略不能为空")
    @Schema(description = "可见性策略 ADD、EDIT、LIST_QUERY、LIST_VIEW")
    private Set<String> policyCrudJson;

    @NotNull(message = "查询策略不能为空")
    @Range(min = 0, max = 1, message = "查询策略值只能为0或1")
    @Schema(description = "查询策略 0:等于")
    private Integer policyQuery;

    @NotNull(message = "显示策略不能为空")
    @Range(min = 0, max = 6, message = "显示策略值范围为0-6")
    @Schema(description = "显示策略 0:文本框 1:文本域 2:下拉 3:单 4:多 5:LD 6:LDT")
    private Integer policyView;

    @NotNull(message = "是否主键不能为空")
    @Range(min = 0, max = 1, message = "是否主键值只能为0或1")
    @Schema(description = "是否主键 0:否 1:是")
    private Integer pk;

    @NotBlank(message = "字段备注不能为空")
    @Size(max = 80, message = "字段备注长度不能超过80")
    @Schema(description = "字段备注")
    private String remark;

    @NotNull(message = "排序不能为空")
    @Schema(description = "排序")
    private Integer seq;

}
