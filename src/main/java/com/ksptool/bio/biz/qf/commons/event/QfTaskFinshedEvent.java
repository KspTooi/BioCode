package com.ksptool.bio.biz.qf.commons.event;

import org.springframework.context.ApplicationEvent;

import lombok.Getter;
import lombok.Setter;

/**
 * 任务结束事件
 * 
 * @author KspTool(ksptool@outlook.com)
 * @since 2026-04-17
 * @license Apache License 2.0
 */
@Getter
@Setter
public class QfTaskFinshedEvent extends ApplicationEvent{

    //业务表单ID
    private Long bizFormId;

    //业务表单编号
    private String bizFormCode;
    
    //业务数据主键ID
    private Long dataId;

    //当前节点名
    private String currentNodeName;

    //下一任务节点名
    private String nextNodeName;

    public QfTaskFinshedEvent(Object source) {
        super(source);
    }

}
