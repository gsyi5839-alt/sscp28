package com.bcbbs.backend.config;

import com.github.benmanes.caffeine.cache.Cache;
import org.junit.jupiter.api.Test;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.caffeine.CaffeineCacheManager;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

class CacheConfigTest {

    @Test
    void lotteryInfoCacheExpiresWithinCountdownTickWindow() {
        CaffeineCacheManager cacheManager = (CaffeineCacheManager) new CacheConfig().cacheManager();
        CaffeineCache springCache = (CaffeineCache) cacheManager.getCache("lotteryInfo");
        assertThat(springCache).isNotNull();

        Cache<Object, Object> nativeCache = springCache.getNativeCache();

        assertThat(nativeCache.policy().expireAfterWrite())
                .isPresent()
                .get()
                .extracting(policy -> policy.getExpiresAfter(TimeUnit.MILLISECONDS))
                .as("lotteryInfo should not keep stale draw issue data longer than one countdown tick")
                .isEqualTo(1000L);
    }
}
