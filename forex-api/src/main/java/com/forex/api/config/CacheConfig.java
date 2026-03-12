package com.forex.api.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

/**
 * Configuración - Cache en memoria para tasas de cambio.
 * Evita llamadas repetidas a la API externa.
 */
@Configuration
@EnableCaching
public class CacheConfig {
    
    @Bean
    public CacheManager cacheManager() {
        SimpleCacheManager cacheManager = new SimpleCacheManager();
        cacheManager.setCaches(Arrays.asList(
            new ConcurrentMapCache(FrankfurterApiConstants.LATEST_RATES_CACHE),
            new ConcurrentMapCache(FrankfurterApiConstants.HISTORICAL_RATES_CACHE)
        ));
        return cacheManager;
    }
}
