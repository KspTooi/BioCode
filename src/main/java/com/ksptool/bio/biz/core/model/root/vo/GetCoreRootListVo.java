package com.ksptool.bio.biz.core.model.root.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class GetCoreRootListVo {

    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "租户名称")
    private String name;

    @Schema(description = "租户下用户数量")
    private Long ruCount;

    @Schema(description = "到期时间(null长期)")
    private LocalDateTime expireTime;

    @Schema(description = "状态 0:正常 1:禁用")
    private Integer status;

    @Schema(description = "管理员账号")
    private String adminUsername;

    @Schema(description = "内置租户 0:否 1:是")
    private Integer isSystem;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

}
