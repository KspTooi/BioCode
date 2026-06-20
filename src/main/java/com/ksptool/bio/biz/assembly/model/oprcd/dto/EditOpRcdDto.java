package com.ksptool.bio.biz.assembly.model.oprcd.dto;

import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import io.swagger.v3.oas.annotations.media.Schema;

@Getter
@Setter
public class EditOpRcdDto {

    @Schema(description="主键ID")
    private Long id;


}
