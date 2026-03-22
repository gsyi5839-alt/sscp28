package com.bcbbs.backend.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * Cache configuration using Caffeine local cache.
 * 
 * Cache regions:
 * - lotteryGames: lottery type catalog, 1h TTL, max 10 entries
 * - lotteryInfo: current issue info per lotCode, 5s TTL, max 20 entries
 * - lotteryListFirstPage: first page history (latest draws), 5s TTL, max 20 entries
 * - lotteryListOtherPages: other pages history (stable data), 15min TTL, max 100 entries
 * - accessLines: access line list per type, 6h TTL, max 5 entries
 */
@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        
        // Lottery games catalog: admin may update, cache 1 hour
        cacheManager.registerCustomCache("lotteryGames",
            Caffeine.newBuilder()
                .expireAfterWrite(1, TimeUnit.HOURS)
                .maximumSize(10)
                .recordStats()
                .build());
        
        // Current issue info: changes every ~5 min, cache 5 seconds to reduce upstream calls
        cacheManager.registerCustomCache("lotteryInfo",
            Caffeine.newBuilder()
                .expireAfterWrite(5, TimeUnit.SECONDS)
                .maximumSize(20)
                .recordStats()
                .build());
        
        // First page history: new draws every 3-5 min, cache 5 seconds
        cacheManager.registerCustomCache("lotteryListFirstPage",
            Caffeine.newBuilder()
                .expireAfterWrite(5, TimeUnit.SECONDS)
                .maximumSize(20)
                .recordStats()
                .build());
        
        // Other pages history: stable historical data, cache 15 minutes
        cacheManager.registerCustomCache("lotteryListOtherPages",
            Caffeine.newBuilder()
                .expireAfterWrite(15, TimeUnit.MINUTES)
                .maximumSize(100)
                .recordStats()
                .build());
        
        // Access lines: rarely changes, cache 6 hours
        cacheManager.registerCustomCache("accessLines",
            Caffeine.newBuilder()
                .expireAfterWrite(6, TimeUnit.HOURS)
                .maximumSize(5)
                .recordStats()
                .build());
        
        return cacheManager;
    }
}
