package com.ksptool.bio.biz.core.common;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * 开关类
 * 用于表示开关的开启和关闭状态
 * 0:关闭 1:开启
 *
 * @author KspTool
 * @since 1.6.21(U).90
 */
public class Switch {

    public static final int ON = 1;
    public static final int OFF = 0;

    private final int value;

    @JsonCreator
    public Switch(int value) {
        this.value = value;
    }

    @JsonValue
    public int getValue() {
        return value;
    }

    public static int on() {
        return ON;
    }

    public static int off() {
        return OFF;
    }

    public static int yes() {
        return ON;
    }

    public static int no() {
        return OFF;
    }

    public static int active() {
        return ON;
    }

    public static int inactive() {
        return OFF;
    }

    public boolean isOn() {
        return value == ON;
    }

    public boolean isOff() {
        return value == OFF;
    }

    public boolean isYes() {
        return value == ON;
    }

    public boolean isNo() {
        return value == OFF;
    }

    public boolean isActive() {
        return value == ON;
    }

    public boolean isInactive() {
        return value == OFF;
    }

}
