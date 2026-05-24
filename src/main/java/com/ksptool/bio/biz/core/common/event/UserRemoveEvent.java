package com.ksptool.bio.biz.core.common.event;

import lombok.Getter;

import java.util.List;

import com.ksptool.bio.biz.core.common.BizEvent;
import com.ksptool.bio.biz.core.model.user.UserPo;

/**
 * 用户删除事件
 * 当用户被删除时触发
 */
@Getter
public class UserRemoveEvent extends BizEvent<List<UserPo>> {

    public UserRemoveEvent(List<UserPo> data) {
        super(data);
    }

    public static UserRemoveEvent of(List<UserPo> data) {
        return new UserRemoveEvent(data);
    }

}
