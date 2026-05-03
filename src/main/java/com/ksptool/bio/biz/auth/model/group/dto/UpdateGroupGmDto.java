package com.ksptool.bio.biz.auth.model.group.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Getter
@Setter
public class UpdateGroupGmDto {

    @Schema(description = "组ID")
    @NotNull(message = "组ID不能为空")
    private Long groupId;

    @Schema(description = "菜单ID列表")
    @NotNull(message = "菜单ID列表不能为空")
    private List<Long> menuIds;

}
