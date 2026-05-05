package com.ksptool.bio.biz.core.model.root.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Getter
@Setter
public class UpdateRootRpDto {

    @Schema(description = "租户ID")
    @NotNull(message = "租户ID不能为空")
    private Long rootId;

    @Schema(description = "菜单包ID列表")
    @NotNull(message = "菜单包ID列表不能为空")
    private List<Long> packIds;

}
