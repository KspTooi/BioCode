package com.ksptool.bio.biz.qf.commons.event;

import lombok.Getter;
import lombok.Setter;

/**
 * 流程结束事件
 * 
 * @author KspTool(ksptool@outlook.com)
 * @since 2026-04-17
 */
@Getter
@Setter
public class QfProcFinishedEvent {

    //业务表单ID
    private Long bizFormId;

    //业务表单编号
    private String bizFormCode;
    
    //业务数据主键ID
    private Long dataId;

}
