package com.ksptool.bio.biz.qf.commons.event;

import lombok.Getter;
import lombok.Setter;
import org.springframework.context.ApplicationEvent;
/**
 * 任务重分配事件
 * <p>
 * 当待办任务被重新分配给新的办理人时发布此事件，
 * 供事件监听方感知整改人/办理人变更并执行后续业务逻辑。
 *
 * @author 周彬(961523633@qq.com)
 * @since 2026-05-22
 * @license Proprietary
 * 版权所有 (c) 2026 KspTool及其贡献者保留所有权利。
 * 未经事先书面许可，严禁任何形式的复制或分发。
 */
@Getter
@Setter
public class QfTaskAssignedEvent  extends ApplicationEvent {
    //业务表单ID
    private Long bizFormId;

    //业务表单编号
    private String bizFormCode;

    //业务数据主键ID
    private Long dataId;

    //当前节点名
    private String currentNodeName;
    //待办ID
    private Long todoId;

    public QfTaskAssignedEvent(Object source) {
        super(source);
    }
}
