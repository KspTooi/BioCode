package com.ksptool.bio.biz.auth.common.mybatis;

/**
 * Mybatis数据权限上下文持有者
 * 用于在Mybatis查询时根据用户权限过滤数据
 *
 * @author KspTool
 * @since 1.6.21(U).90
 */
public class RsContextHolder {

    private static final ThreadLocal<RsContext> HOLDER = new ThreadLocal<>();

    public static void set(RsContext context) {
        HOLDER.set(context);
    }

    public static RsContext get() {
        return HOLDER.get();
    }

    public static void clear() {
        HOLDER.remove();
    }
}