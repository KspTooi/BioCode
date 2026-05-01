package com.ksptool.bio.biz.qf.commons.event;

import lombok.Getter;
import lombok.Setter;
import org.springframework.context.ApplicationEvent;

/**
 * 流程启动事件
 *
 * @author KspTool(ksptool@outlook.com)
 * @license Proprietary
 * 版权所有 (c) 2026 KspTool及其贡献者保留所有权利。
 * 未经事先书面许可，严禁任何形式的复制或分发。
 * @since 2026-04-20
 */
@Getter
@Setter
public class QfProcStartedEvent extends ApplicationEvent {




    public QfProcStartedEvent(Object source) {
        super(source);
    }

}
