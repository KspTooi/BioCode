package com.ksptool.bio.biz.qf.model.qfmodel.dto;

import lombok.Getter;
import lombok.Setter;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import com.ksptool.bio.biz.core.common.aop.DtoCustomValidator;
import com.ksptool.bio.biz.qf.commons.qfe.QfeBpmnModel;

@Getter
@Setter
public class DesignQfModelDto implements DtoCustomValidator {

    @NotNull(message = "流程模型ID不能为空")
    @Schema(description = "流程模型ID")
    private Long id;

    @NotBlank(message = "BPMN XML不能为空")
    @Schema(description = "BPMN XML")
    private String bpmnXml;

    @Override
    public String validate() {

        //校验前端设计器设计的模型
        var qbm = new QfeBpmnModel().of(bpmnXml);
        var result = qbm.validateUserTasks();
        
        if (result != null) {
            return result;
        }

        return null;
    }

}
