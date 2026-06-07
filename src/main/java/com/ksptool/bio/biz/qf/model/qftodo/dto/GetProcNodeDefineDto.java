package com.ksptool.bio.biz.qf.model.qftodo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/**
 * 获取流程节点配置DTO
 */
@Getter
@Setter
public class GetProcNodeDefineDto {

    @NotBlank(message = "模型编码不能为空")
    @Schema(description = "模型编码")
    private String code;

}