package com.ksptool.bio.biz.core.common.event;

import java.util.List;

import lombok.Getter;
import com.ksptool.bio.biz.core.common.BizEvent;
import com.ksptool.bio.biz.core.model.user.UserPo;

/**
 * 用户创建事件
 * 当用户被创建时触发
 */
@Getter
public class UserCreateEvent extends BizEvent<List<UserPo>> {

    public UserCreateEvent(List<UserPo> data) {
        super(data);
    }

    public static UserCreateEvent of(List<UserPo> data) {
        return new UserCreateEvent(data);
    }

}
