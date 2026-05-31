package com.ksptool.bio.biz.qf.model.qfbizformfield.dto;

import lombok.Getter;
import lombok.Setter;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Getter
@Setter
public class AddQfBizFormFieldDto {

    @Schema(description="业务表ID")
    private Long formId;

    @Schema(description="字段名")
    @NotBlank(message = "字段名不能为空")
    @Size(max = 32, message = "字段名长度不能超过32")
    private String fieldName;

    @Schema(description="备注")
    @NotBlank(message = "备注不能为空")
    @Size(max = 32, message = "备注长度不能超过32")
    private String remark;

}
