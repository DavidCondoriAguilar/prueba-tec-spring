package com.forex.api.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

/**
 * Cache configuration for the Forex API application.
 * 
 * This class configures Spring Cache with in-memory caching using ConcurrentMapCache.
 * Provides caching strategy to minimize external API calls and improve response times.
 * 
 * @author Forex API Team
 * @version 1.0
 * @since 2026-02-02
 */
@Configuration
@EnableCaching
public class CacheConfig {
    
    /**
     * Configures and returns a SimpleCacheManager with predefined caches.
     * 
     * Caches configured:
     * - forexRates: Caches latest forex rates by base currency and symbols
     * - historicalRates: Caches historical forex rates by date, base currency, and symbols
     * 
     * @return CacheManager instance with configured in-memory caches
     */
    @Bean
    public CacheManager cacheManager() {
        SimpleCacheManager cacheManager = new SimpleCacheManager();
        
        // Configure in-memory caches for different data types using constants
        cacheManager.setCaches(Arrays.asList(
            new ConcurrentMapCache(FrankfurterApiConstants.LATEST_RATES_CACHE),      // Cache for latest rates
            new ConcurrentMapCache(FrankfurterApiConstants.HISTORICAL_RATES_CACHE)  // Cache for historical rates
        ));
        
        return cacheManager;
    }
}
