package com.ksptool.bio.biz.qf.model.qfbizformfield.vo;

import lombok.Getter;
import lombok.Setter;
import io.swagger.v3.oas.annotations.media.Schema;

@Getter
@Setter
public class GetQfBizFormFieldListVo {

    @Schema(description="主键ID")
    private Long id;

    @Schema(description="业务表ID")
    private Long formId;

    @Schema(description="字段名")
    private String fieldName;

    @Schema(description="备注")
    private String remark;

}
