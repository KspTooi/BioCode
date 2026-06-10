package com.ksptool.bio.biz.aacp.model.capability.dto;

import com.ksptool.bio.biz.core.common.aop.DtoCustomValidator;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

import java.util.List;

@Getter
@Setter
public class EditAacpCapabilityDto implements DtoCustomValidator {

    @NotNull(message = "主键ID不能为空")
    @Schema(description = "主键ID")
    private Long id;

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

    @NotNull(message = "微函数ID列表不能为空")
    @Schema(description = "微函数ID列表")
    private List<Long> funcIds;

    @Override
    public String validate() {
        if (funcIds.size() > 50) {
            return "一个能力包最多绑定50个微函数";
        }
        return null;
    }
}
