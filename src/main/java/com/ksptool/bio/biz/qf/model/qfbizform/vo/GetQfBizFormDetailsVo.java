package com.ksptool.bio.biz.qf.model.qfbizform.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GetQfBizFormDetailsVo {

    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "业务名称")
    private String name;

    @Schema(description = "业务编码")
    private String code;

    @Schema(description = "表单类型 0:手搓表单 1:动态表单")
    private Integer formType;

    @Schema(description = "表单图标")
    private String icon;

    @Schema(description = "物理表名")
    private String tableName;

    @Schema(description = "PC端路由名")
    private String routePc;

    @Schema(description = "移动端路由名")
    private String routeMobile;

    @Schema(description = "状态 0:正常 1:停用")
    private Integer status;

    @Schema(description = "排序")
    private Integer seq;

}
