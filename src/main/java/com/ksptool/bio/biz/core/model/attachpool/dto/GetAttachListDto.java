package com.ksptool.bio.biz.core.model.attachpool.dto;

import com.ksptool.assembly.entity.web.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GetAttachListDto extends PageQuery {

    @Schema(description = "文件业务类型")
    private String kind;

    @Schema(description = "状态 0:预检文件 1:区块不完整 2:校验中 3:有效")
    private Integer status;

}
