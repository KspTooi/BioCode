package com.ksptool.bio.biz.core.model.pack.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Getter
@Setter
public class UpdatePackMenuDto {

    @Schema(description = "菜单包ID")
    @NotNull(message = "菜单包ID不能为空")
    private Long packId;

    @Schema(description = "菜单ID列表")
    @NotNull(message = "菜单ID列表不能为空")
    private List<Long> menuIds;

}
