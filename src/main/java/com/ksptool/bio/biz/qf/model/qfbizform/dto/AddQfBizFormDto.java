package com.ksptool.bio.biz.qf.model.qfbizform.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.Range;

@Getter
@Setter
public class AddQfBizFormDto {

    @NotBlank(message = "业务名称不能为空")
    @Length(max = 40, message = "业务名称最多40个字符")
    @Schema(description = "业务名称")
    private String name;

    @NotBlank(message = "业务编码不能为空")
    @Length(max = 16, message = "业务编码最多16个字符")
    @Schema(description = "业务编码")
    private String code;

    @NotNull(message = "表单类型不能为空")
    @Range(min = 0, max = 1, message = "表单类型只能为0(手搓表单)或1(动态表单)")
    @Schema(description = "表单类型 0:手搓表单 1:动态表单")
    private Integer formType;

    @NotBlank(message = "表单图标不能为空")
    @Length(max = 80, message = "表单图标最多80个字符")
    @Schema(description = "表单图标")
    private String icon;

    @NotBlank(message = "物理表名不能为空")
    @Length(max = 200, message = "物理表名最多200个字符")
    @Schema(description = "物理表名")
    private String tableName;

    @Length(max = 512, message = "PC端路由名最多512个字符")
    @Schema(description = "PC端路由名")
    private String routePc;

    @Length(max = 512, message = "移动端路由名最多512个字符")
    @Schema(description = "移动端路由名")
    private String routeMobile;

    @NotNull(message = "状态不能为空")
    @Range(min = 0, max = 1, message = "状态只能为0(正常)或1(停用)")
    @Schema(description = "状态 0:正常 1:停用")
    private Integer status;

    @NotNull(message = "排序不能为空")
    @Schema(description = "排序")
    private Integer seq;

}
