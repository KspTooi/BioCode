package com.ksptool.bio.biz.audit.model.auditlogin.dto;

import com.ksptool.assembly.entity.web.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GetAuditLoginListDto extends PageQuery {

    @Schema(description = "用户账号")
    private String username;

    @Schema(description = "状态: 0:成功 1:失败")
    private Integer status;

}

