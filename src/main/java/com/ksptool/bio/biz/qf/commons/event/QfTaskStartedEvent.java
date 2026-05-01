package com.ksptool.bio.biz.qf.commons.event;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

import org.springframework.context.ApplicationEvent;

/**
 * 任务启动事件
 *
 * @author KspTool(ksptool@outlook.com)
 * @license Proprietary
 * 版权所有 (c) 2026 KspTool及其贡献者保留所有权利。
 * 未经事先书面许可，严禁任何形式的复制或分发。
 * @since 2026-04-20
 */
@Getter
@Setter
public class QfTaskStartedEvent extends ApplicationEvent {

    //发起人租户ID
    private Long rootId;

    //发起人部门ID
    private Long deptId;

    //引擎任务ID
    private String engTaskId;

    //引擎流程ID
    private String engProcId;

    //待办ID
    private Long todoId;

    //业务表单ID
    private Long bizFormId;

    //业务表名
    private String tableName;

    //业务数据主键ID
    private Long dataId;

    //当前节点名
    private String nodeName;

    //摘要
    private String summary;

    //办理成员类型 0:用户, 1:用户组
    private Integer memberType;

    //办理成员ID
    private Long memberId;

    //发起人ID
    private Long initiatorId;

    //发起人姓名
    private String initiatorName;

    //发起时间
    private LocalDateTime initiatorTime;

    /**
     * 构造函数
     * @param source 事件源
     */
    public QfTaskStartedEvent(Object source) {
        super(source);
    }

}
