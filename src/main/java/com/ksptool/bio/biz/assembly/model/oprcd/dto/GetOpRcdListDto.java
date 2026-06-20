package com.ksptool.bio.biz.assembly.model.oprcd.dto;

import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import com.ksptool.assembly.entity.web.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * @author KspTooi
 * @since 1.7.10(J).1
 */
@Getter
@Setter
public class GetOpRcdListDto extends PageQuery {

    @Schema(description="输出方案名称")
    private String opName;

    @Schema(description="数据源名称")
    private String dsName;

    @Schema(description="数据源表名")
    private String dsTableName;

    @Schema(description="模型名称")
    private String modelName;

    @Schema(description="业务域")
    private String bizDomain;

    @Schema(description="操作人账号")
    private String creatorUsername;

}
