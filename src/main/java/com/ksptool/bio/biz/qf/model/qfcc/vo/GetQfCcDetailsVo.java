package com.ksptool.bio.biz.qf.model.qfcc.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GetQfCcDetailsVo {

    @Schema(description = "引擎流程ID")
    private String engProcId;

    @Schema(description = "业务表单ID")
    private Long bizFormId;

    @Schema(description = "物理表名(带入业务表单数据)")
    private String tableName;

    @Schema(description = "物理表数据主键ID")
    private Long dataId;

    @Schema(description = "当前节点名称 (如: 财务总监审批)")
    private String nodeName;

    @Schema(description = "摘要(如：张三提交的 5000 元报销)")
    private String summary;

    @Schema(description = "抄送发起人ID(自动抄送为null)")
    private Long fromId;

    @Schema(description = "抄送发起人姓名")
    private String fromName;

    @Schema(description = "是否读 0:未读 1:已读")
    private Integer isRead;

}
