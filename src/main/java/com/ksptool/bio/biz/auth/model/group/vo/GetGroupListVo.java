package com.ksptool.bio.biz.auth.model.group.vo;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;

@Getter
@Setter
public class GetGroupListVo {

    @Schema(description = "组ID")
    private Long id;

    @Schema(description = "组标识")
    private String code;

    @Schema(description = "组名称")
    private String name;

    @Schema(description = "成员数量")
    private Integer memberCount;

    @Schema(description = "权限码数量")
    private Integer permissionCount;

    @Schema(description = "RS数据权限等级 0:全集团 10:本公司+下级公司 20:仅本公司 30:本部门+下级部门 40:仅本部门 50:仅本人 60:指定组织")
    private Integer rowScope;

    @Schema(description = "系统内置组 0:否 1:是")
    private Integer isSystem;

    @Schema(description = "组状态：0-禁用，1-启用")
    private Integer status;
    
    @Schema(description = "排序号")
    private Integer seq;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

}
