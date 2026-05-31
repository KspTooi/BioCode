package com.ksptool.bio.biz.qf.model.qfmodel.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GetQfModelDetailsVo {

    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "模型分组ID")
    private Long groupId;
    @Schema(description = "关联表单ID")
    private Long formId;
    @Schema(description = "模型名称")
    private String name;

    @Schema(description = "模型编码")
    private String code;

    @Schema(description = "模型BPMN XML")
    private String bpmnXml;

    @Schema(description = "排序")
    private Integer seq;

}
