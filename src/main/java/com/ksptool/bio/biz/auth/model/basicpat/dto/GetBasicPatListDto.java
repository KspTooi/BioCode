package com.ksptool.bio.biz.auth.model.basicpat.dto;

import com.ksptool.assembly.entity.web.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GetBasicPatListDto extends PageQuery {

    @Schema(description = "PAT名称")
    private String name;

    @Schema(description = "状态: 0:禁用 1:启用")
    private Integer status;

}
