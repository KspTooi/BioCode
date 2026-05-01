package com.ksptool.bio.biz.core.model.org.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class GetOrgListVo {

    @Schema(description = "主键id")
    private Long id;

    @Schema(description = "顶级组织ID")
    private Long topId;

    @Schema(description = "上级组织ID NULL顶级组织")
    private Long parentId;

    @Schema(description = "0:企业(租户) 1:子企业 2:部门 3:班组")
    private Integer kind;

    @Schema(description = "组织机构名称")
    private String name;

    @Schema(description = "行业标识ID")
    private Long industryId;

    @Schema(description = "行业标识名称")
    private String industryName;

    @Schema(description = "主管ID")
    private Long principalId;

    @Schema(description = "主管名称")
    private String principalName;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "创建人id")
    private Long creatorId;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    @Schema(description = "更新人id")
    private Long updaterId;

    @Schema(description = "删除时间 NULL未删除")
    private LocalDateTime deleteTime;

}

