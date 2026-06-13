package com.ksptool.bio.biz.aacp.model.func.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Schema(description = "已注册微函数")
public class GetMicroFuncRegistryVo {

    @Schema(description = "微函数标识")
    private String target;

    @Schema(description = "微函数名称")
    private String name;

    @Schema(description = "微函数描述")
    private String description;

    @Schema(description = "参数数量")
    private int parameterCount;

    @Schema(description = "参数类型列表（全限定类名）")
    private List<String> parameterTypes;
}
