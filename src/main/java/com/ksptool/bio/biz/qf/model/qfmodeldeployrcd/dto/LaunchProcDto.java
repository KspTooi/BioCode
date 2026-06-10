package com.ksptool.bio.biz.qf.model.qfmodeldeployrcd.dto;

import java.util.Set;

import com.ksptool.bio.biz.qf.commons.LaunchParam;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LaunchProcDto {

    @NotBlank(message = "模型编码不能为空")
    private String code;

    @NotNull(message = "业务数据ID不能为空")
    private Long dataId;

    @NotNull(message = "启动成员参数列表不能为空")
    @Schema(description = "启动成员参数列表")
    private Set<LaunchParam.LaunchMemberParam> members;


}
