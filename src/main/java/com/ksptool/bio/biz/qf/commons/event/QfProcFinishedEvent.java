package com.ksptool.bio.biz.qf.commons.event;

import org.springframework.context.ApplicationEvent;

import lombok.Getter;
import lombok.Setter;

/**
 * 流程结束事件
 * 
 * @author KspTool(ksptool@outlook.com)
 * @since 2026-04-17
 * @license Proprietary
 * 版权所有 (c) 2026 KspTool及其贡献者保留所有权利。
 * 未经事先书面许可，严禁任何形式的复制或分发。
 */
@Getter
@Setter
public class QfProcFinishedEvent extends ApplicationEvent{

    //业务表单ID
    private Long bizFormId;

    //业务表单编号
    private String bizFormCode;
    
    //业务数据主键ID
    private Long dataId;
    
    public QfProcFinishedEvent(Object source) {
        super(source);
    }

}
