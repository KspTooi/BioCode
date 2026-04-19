package com.ksptool.bio.biz.qfcc.model.vo;

import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import io.swagger.v3.oas.annotations.media.Schema;

@Getter
@Setter
public class GetQfCcListVo {

    @Schema(description="主键ID")
    private Long id;

    @Schema(description="当前节点名称 (如: 财务总监审批)")
    private String nodeName;

    @Schema(description="摘要(如：张三提交的 5000 元报销)")
    private String summary;

    @Schema(description="抄送发起人姓名")
    private String fromName;

    @Schema(description="是否读 0:未读 1:已读")
    private Integer isRead;

    @Schema(description="读取时间")
    private LocalDateTime readTime;

    @Schema(description="抄送时间")
    private LocalDateTime createTime;

}
