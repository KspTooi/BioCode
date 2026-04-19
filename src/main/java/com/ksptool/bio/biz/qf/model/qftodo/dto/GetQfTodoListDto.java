package com.ksptool.bio.biz.qf.model.qftodo.dto;

import com.ksptool.assembly.entity.web.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Range;

@Getter
@Setter
public class GetQfTodoListDto extends PageQuery {

    @Schema(description = "待办名称")
    private String nodeName;

    @Schema(description = "业务表单ID")
    private Long bizFormId;

    @Range(min = 0, max = 1,message = "待办状态只能为0或1")
    @Schema(description = "待办状态 0:待办 1:已办")
    private Integer status;

}
