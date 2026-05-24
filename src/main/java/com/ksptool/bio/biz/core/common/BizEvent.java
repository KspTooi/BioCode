package com.ksptool.bio.biz.core.common;

import lombok.Getter;
import lombok.Setter;
import org.springframework.context.ApplicationEvent;

/**
 * 业务事件
 * 所有业务事件都应该继承这个类
 */
public abstract class BizEvent<T> extends ApplicationEvent {

    //事件数据
    private T data;

    public BizEvent(T data) {
        super(data);
    }


}
