package com.ksptool.bio.biz.qf.commons.enums;

import lombok.Getter;

/**
 * 待办状态（QfTodoPo.status）
 */
@Getter
public enum TodoStatus {
    /**
     * 待办（待处理）
     */
    PENDING(0),

    /**
     * 已办
     */
    DONE(1),

    /**
     * 已取消（作废）
     */
    CANCELLED(10);

    private final int value;

    TodoStatus(int value) {
        this.value = value;
    }
}
