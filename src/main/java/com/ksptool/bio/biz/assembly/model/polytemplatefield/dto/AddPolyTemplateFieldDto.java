package com.ksptool.bio.biz.assembly.model.polytemplatefield.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Range;

@Getter
@Setter
public class AddPolyTemplateFieldDto {

    @NotNull(message = "聚合模板ID不能为空")
    @Schema(description = "聚合模板ID")
    private Long polyTemplateId;

    @NotBlank(message = "字段名不能为空")
    @Size(max = 255, min = 1, message = "字段名长度必须在1-255个字符之间")
    @Schema(description = "字段名")
    private String name;

    @NotBlank(message = "可见性策略不能为空")
    @Size(max = 65535, message = "可见性策略长度不能超过65535个字符")
    @Schema(description = "可见性策略 ADD、EDIT、LIST_QUERY、LIST_VIEW")
    private String policyCrudJson;

    @NotNull(message = "查询策略不能为空")
    @Schema(description = "查询策略 0:等于")
    private Integer policyQuery;

    @NotNull(message = "显示策略不能为空")
    @Schema(description = "显示策略 0:文本框 1:文本域 2:下拉 3:单 4:多 5:LD 6:LDT")
    private Integer policyView;

    @NotNull(message = "排序不能为空")
    @Range(min = 0, message = "排序不能小于0")
    @Schema(description = "排序")
    private Integer seq;

}
