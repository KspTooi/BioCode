package com.ksptool.bio.biz.auth.model.group.dto;

import lombok.Getter;
import lombok.Setter;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.util.List;

@Getter
@Setter
public class UpdateGroupGpDto {

    @Schema(description = "组ID")
    @NotNull(message = "组ID不能为空")
    private Long groupId;

    @Schema(description = "权限ID列表")
    @NotNull(message = "权限ID列表不能为空")
    private List<Long> permissionIds;

}
