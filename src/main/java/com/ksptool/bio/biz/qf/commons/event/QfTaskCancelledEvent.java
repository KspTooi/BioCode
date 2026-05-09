package com.ksptool.bio.biz.qf.commons.event;

import lombok.Getter;
import lombok.Setter;
import org.springframework.context.ApplicationEvent;

/**
 * 任务取消事件
 *
 * @author KspTool(ksptool@outlook.com)
 * @license Proprietary
 * 版权所有 (c) 2026 KspTool及其贡献者保留所有权利。
 * 未经事先书面许可，严禁任何形式的复制或分发。
 * @since 2026-05-09
 */
@Getter
@Setter
public class QfTaskCancelledEvent extends ApplicationEvent {

    //业务表单ID
    private Long bizFormId;

    //业务表单编号
    private String bizFormCode;

    //业务数据主键ID
    private Long dataId;

    //当前节点名
    private String currentNodeName;

    //取消原因
    private String reason;

    public QfTaskCancelledEvent(Object source) {
        super(source);
    }

}
