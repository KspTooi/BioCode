package com.ksptool.bio.biz.core.common.event;

import java.util.List;

import com.ksptool.bio.biz.core.common.BizEvent;
import com.ksptool.bio.biz.core.model.root.CoreRootPo;

import lombok.Getter;

/**
 * 租户创建事件
 * 当租户被创建时触发
 */
@Getter
public class RootCreateEvent extends BizEvent<List<CoreRootPo>> {

    public RootCreateEvent(List<CoreRootPo> data) {
        super(data);
    }

    public static RootCreateEvent of(List<CoreRootPo> data) {
        return new RootCreateEvent(data);
    }

}
