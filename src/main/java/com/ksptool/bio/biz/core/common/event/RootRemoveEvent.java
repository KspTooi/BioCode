package com.ksptool.bio.biz.core.common.event;

import java.util.ArrayList;
import java.util.List;

import com.ksptool.bio.biz.core.common.BizEvent;
import com.ksptool.bio.biz.core.model.root.CoreRootPo;

import lombok.Getter;

/**
 * 租户删除事件
 * 当租户被删除时触发
 */
@Getter
public class RootRemoveEvent extends BizEvent<List<CoreRootPo>> {

    public RootRemoveEvent(List<CoreRootPo> data) {
        super(data);
    }

    public static RootRemoveEvent of(List<CoreRootPo> data) {
        return new RootRemoveEvent(data);
    }

}
