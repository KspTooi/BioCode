package com.ksptool.bio.biz.core.model.user.dto;


import com.ksptool.assembly.entity.web.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
public class GetUserListDto extends PageQuery {

    @Schema(description = "所属租户名称")
    private String rootName;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "昵称")
    private String nickname;

    @Schema(description = "手机号")
    private String phone;

    @Schema(description = "组织架构ID")
    private Long orgId;

    @Schema(description = "0:封禁 1:正常")
    private Integer status;

    @Schema(description = "用户ID集合")
    private Set<Long> userIds;

}
