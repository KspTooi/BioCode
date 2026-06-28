package com.ksptool.bio.biz.core.model.attachpool.dto;

import com.ksptool.assembly.entity.web.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GetAttachListDto extends PageQuery {

    @Schema(description = "文件业务类型")
    private String kind;

    @Schema(description = "索引筛选 1:已索引 0:无效")
    private Integer indexFilter;

}
