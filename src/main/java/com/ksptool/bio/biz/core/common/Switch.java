package com.ksptool.bio.biz.core.common;

/**
 * 开关类
 * 用于表示开关的开启和关闭状态
 * 0:关闭 1:开启 
 */
public class Switch {

    public static final int ON = 1;
    public static final int OFF = 0;

    public static int on() {
        return ON;
    }
    public static int off() {
        return OFF;
    }
    public static int yes(){
        return ON;
    }
    public static int no(){
        return OFF;
    }
    public static int active(){
        return ON;
    }
    public static int inactive(){
        return OFF;
    }



}
