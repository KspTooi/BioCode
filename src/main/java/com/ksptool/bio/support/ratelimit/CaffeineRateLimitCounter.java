package com.ksptool.bio.support.ratelimit;

import com.github.benmanes.caffeine.cache.Cache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnMissingBean(name = "stringRedisTemplate")
public class CaffeineRateLimitCounter implements RateLimitCounter {

    @Autowired
    private CacheManager cacheManager;

    @Override
    public long incrementAndGet(String cacheName, String key, long ttlSeconds) {
        var cache = cacheManager.getCache(cacheName);
        if (cache == null) {
            throw new IllegalStateException("rateLimit cache 不存在: " + cacheName);
        }
        if (!(cache instanceof CaffeineCache caffeineCache)) {
            throw new IllegalStateException("cache 不是 CaffeineCache: " + cacheName);
        }

        Cache<Object, Object> nativeCache = caffeineCache.getNativeCache();

        Object vObj = nativeCache.asMap().merge(key, 1L, (a, b) -> {
            if (a == null) {
                return b;
            }
            return (Long) a + (Long) b;
        });
        if (vObj == null) {
            return 1L;
        }
        return (Long) vObj;
    }
}