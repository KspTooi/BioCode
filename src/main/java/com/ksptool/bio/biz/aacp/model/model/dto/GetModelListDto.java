package com.ksptool.bio.biz.aacp.model.model.dto;

import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import com.ksptool.assembly.entity.web.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GetModelListDto extends PageQuery {

    @Schema(description="模型变体名称")
    private String name;

    @Schema(description="模型标识")
    private String code;

    @Schema(description="类型 0:文本 1:图形 2:音频 3:多模态")
    private Integer kind;

    @Schema(description="状态 0:禁用 1:启用")
    private Integer status;

}
