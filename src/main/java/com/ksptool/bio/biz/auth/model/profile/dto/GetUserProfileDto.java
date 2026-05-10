package com.ksptool.bio.biz.auth.model.profile.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GetUserProfileDto {

    @Schema(description = "是否强制刷新缓存 0:否 1:是")
    private Integer forceUpdate;

}
