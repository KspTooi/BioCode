package com.ksptool.bio.biz.aacp.model.cap.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GetCapListVo {

    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "能力包名称")
    private String name;

    @Schema(description = "类型 0:微函数")
    private Integer kind;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "关联微函数数量")
    private Long funcCount;

}
