package com.ksptool.bio.biz.qfcc.model.dto;

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
public class GetQfCcListDto extends PageQuery {

    @Schema(description="摘要(如：张三提交的 5000 元报销)")
    private String summary;

    @Schema(description="抄送发起人姓名")
    private String fromName;

    @Schema(description="是否读 0:未读 1:已读")
    private Integer isRead;

}
