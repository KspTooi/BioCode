package com.ksptool.bio.biz.aacp.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

@Getter
@Setter
public class AddAacpCapabilityDto {

    @NotBlank(message = "能力包名称不能为空")
    @Length(max = 40, message = "能力包名称长度不能超过40")
    @Schema(description = "能力包名称")
    private String name;

    @NotNull(message = "类型不能为空")
    @Schema(description = "类型 0:微函数")
    private Integer kind;

    @Length(max = 500, message = "备注长度不能超过500")
    @Schema(description = "备注")
    private String remark;

}
