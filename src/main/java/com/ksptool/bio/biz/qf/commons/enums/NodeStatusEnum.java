package com.ksptool.bio.biz.qf.commons.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum NodeStatusEnum {
    // stroke: 深色边框  fill: 浅色背景，保证节点内文字可读
    FINISHED("FINISHED", "已完成", "#67C23A", "#F0F9EB"),
    CURRENT("CURRENT", "当前节点", "#409EFF", "#ECF5FF"),
    PENDING("PENDING", "未审批", "#E6A23C", "#FDF6EC");

    private final String code;
    private final String name;
    /** bioc:stroke 颜色（边框） */
    private final String stroke;
    /** bioc:fill 颜色（背景） */
    private final String fill;
}
