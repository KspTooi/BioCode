package com.ksptool.bio.biz.qf.commons;

import lombok.Getter;

/**
 * 办理成员类型
 *
 * @author (Ish)Yuumi(1144150092@qq.com)
 * @license Proprietary
 * 版权所有 (c) 2026 KspTool及其贡献者保留所有权利。
 * 未经事先书面许可，严禁任何形式的复制或分发。
 * @since 2026-04-15
 */
@Getter
public enum QfMemberKinds {

    //用户(具体用户办理) 前端传入assigneeKind="user"
    USER(0),

    //用户组(由这个用户组下的所有用户办理) 前端传入assigneeKind="group"
    GROUP(1);

    /**
     * 办理成员类型
     */
    private final int value;

    QfMemberKinds(int value) {
        this.value = value;
    }

}
