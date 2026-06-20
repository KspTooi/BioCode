package com.ksptool.bio.biz.polytemplate.model.vo;

import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import io.swagger.v3.oas.annotations.media.Schema;

@Getter
@Setter
public class GetPolyTemplateDetailsVo {

    @Schema(description="主键ID")
    private Long id;

    @Schema(description="模板名称")
    private String name;

    @Schema(description="模板代码")
    private String code;

    @Schema(description="排序")
    private Integer seq;

    @Schema(description="状态 0:禁用 1:启用")
    private Integer status;

}
