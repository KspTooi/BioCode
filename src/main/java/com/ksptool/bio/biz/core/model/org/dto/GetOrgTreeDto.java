package com.ksptool.bio.biz.core.model.org.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GetOrgTreeDto {

    @Schema(description = "组织机构名称")
    private Long rootId;

    @Schema(description = "组织机构名称")
    private String name;

}

