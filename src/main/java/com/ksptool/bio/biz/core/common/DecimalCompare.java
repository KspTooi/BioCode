package com.ksptool.bio.biz.core.common;

import java.math.BigDecimal;

/**
 * @author KspTooi
 * @since 1.6.14(N).45
 */
public class DecimalCompare {

    /**
     * 判断给定的值是否小于阈值
     *
     * @param target    值
     * @param threshold 阈值
     * @return 是否小于
     */
    public static boolean isLessThan(BigDecimal target, BigDecimal threshold) {

        if (target == null || threshold == null) {
            return false;
        }

        return target.compareTo(threshold) < 0;
    }

    /**
     * 判断给定的值是否大于阈值
     *
     * @param target    值
     * @param threshold 阈值
     * @return 是否大于
     */
    public static boolean isGreaterThan(BigDecimal target, BigDecimal threshold) {

        if (target == null || threshold == null) {
            return false;
        }

        return target.compareTo(threshold) > 0;
    }

    /**
     * 判断给定的值是否等于阈值
     *
     * @param target    值
     * @param threshold 阈值
     * @return 是否等于
     */
    public static boolean isEqualTo(BigDecimal target, BigDecimal threshold) {

        if (target == null || threshold == null) {
            return false;
        }

        return target.compareTo(threshold) == 0;
    }
}
