package com.ksptool.bio.biz.qf.model.qfmodeldeployrcd.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LaunchQfProcessDto {

    @NotBlank(message = "模型编码不能为空")
    private String code;

    @NotBlank(message = "业务表单编码不能为空")
    private String bizFormCode;

    @NotNull(message = "业务数据ID不能为空")
    private Long dataId;

}
