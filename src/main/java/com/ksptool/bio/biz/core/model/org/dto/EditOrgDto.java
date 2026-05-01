package com.ksptool.bio.biz.core.model.org.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

@Getter
@Setter
public class EditOrgDto{

    @NotNull(message = "主键id不能为空")
    @Schema(description = "主键id")
    private Long id;

    @Schema(description = "上级组织ID NULL顶级组织")
    private Long parentId;

    @NotNull(message = "组织机构名称不能为空")
    @Size(max = 80, message = "组织机构名称长度不能超过80个字符")
    @Schema(description = "组织机构名称")
    private String name;

    @Length(max = 40, message = "组织机构简称长度不能超过40个字符")
    @Schema(description = "组织机构简称")
    private String shortName;

    @NotNull(message = "排序不能为空")
    @Schema(description = "排序")
    private Integer seq;

    @Schema(description = "备注")
    @Length(max = 200, message = "备注长度不能超过200个字符")
    private String remark;

}

