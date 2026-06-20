package com.ksptool.bio.biz.assembly.model.polytemplate.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class GetPolyTemplateListVo {

    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "模板名称")
    private String name;

    @Schema(description = "模板代码")
    private String code;

    @Schema(description = "排序")
    private Integer seq;

    @Schema(description = "状态 0:禁用 1:启用")
    private Integer status;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

}
