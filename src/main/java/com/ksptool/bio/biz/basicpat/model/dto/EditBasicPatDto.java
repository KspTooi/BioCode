package com.ksptool.bio.biz.basicpat.model.dto;

import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import io.swagger.v3.oas.annotations.media.Schema;

@Getter
@Setter
public class EditBasicPatDto {

    @Schema(description="主键ID")
    private Long id;


    @Schema(description="PAT名称")
    private String name;

    @Schema(description="状态: 0:禁用 1:启用")
    private Integer status;

}
