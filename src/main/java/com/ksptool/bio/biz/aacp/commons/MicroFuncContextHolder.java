package com.ksptool.bio.biz.aacp.commons;

/**
 * 微函数上下文持有者：通过 ThreadLocal 传递当前调用的 Agent Hub ID。
 * <p>
 * 在 AacpAccessService.inbound() 中调用 runtimeService.call() 之前设置，
 * 微函数方法内部通过 get() 获取当前所属 Hub，用于 Cap 权限校验等场景。
 * call() 返回后由 AacpAccessService 负责清理。
 */
public class MicroFuncContextHolder {

    private static final ThreadLocal<Long> HUB_ID_HOLDER = new ThreadLocal<>();

    public static void set(Long hubId) {
        HUB_ID_HOLDER.set(hubId);
    }

    public static Long get() {
        return HUB_ID_HOLDER.get();
    }

    public static void clear() {
        HUB_ID_HOLDER.remove();
    }
}