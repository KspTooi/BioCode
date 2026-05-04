package com.ksptool.bio.biz.package.model.vo;

import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import io.swagger.v3.oas.annotations.media.Schema;

@Getter
@Setter
public class GetPackageDetailsVo {

    @Schema(description="主键ID")
    private Long id;

    @Schema(description="菜单包名")
    private String name;

    @Schema(description="菜单包编码")
    private String code;

    @Schema(description="状态 0:禁用 1:启用")
    private Integer status;

    @Schema(description="排序")
    private Integer seq;

    @Schema(description="备注")
    private String remark;

}
