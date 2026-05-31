package com.ksptool.bio.biz.qf.model.qfbizformfield.dto;

import com.ksptool.assembly.entity.web.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GetQfBizFormFieldListDto extends PageQuery {
    @Schema(description="业务表ID")
    private Long formId;
}
