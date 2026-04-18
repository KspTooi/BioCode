package com.ksptool.bio.biz.qf.model.qfmodeldeployrcd.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class LaunchQfProcessDto {

    @NotBlank(message = "模型编码不能为空")
    private String code;

    private String businessKey;

    private Map<String, Object> variables;

}
