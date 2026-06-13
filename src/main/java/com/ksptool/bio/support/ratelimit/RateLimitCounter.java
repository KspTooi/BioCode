package com.ksptool.bio.support.ratelimit;

public interface RateLimitCounter {

    /**
     * 增加计数并获取当前计数
     *
     * @param cacheName  缓存名称
     * @param key        缓存键
     * @param ttlSeconds 缓存过期时间
     * @return 当前计数
     */
    long incrementAndGet(String cacheName, String key, long ttlSeconds);

}