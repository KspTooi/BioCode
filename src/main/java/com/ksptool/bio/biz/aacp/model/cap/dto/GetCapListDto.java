package com.ksptool.bio.biz.aacp.model.cap.dto;

import com.ksptool.assembly.entity.web.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GetCapListDto extends PageQuery {

    @Schema(description = "能力包名称")
    private String name;

    @Schema(description = "类型 0:标准")
    private Integer kind;

}
