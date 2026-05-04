package com.ksptool.bio.biz.package.model.dto;

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
public class GetPackageListDto extends PageQuery {

    @Schema(description="菜单包名")
    private String name;

    @Schema(description="菜单包编码")
    private String code;

    @Schema(description="状态 0:禁用 1:启用")
    private Integer status;

}
