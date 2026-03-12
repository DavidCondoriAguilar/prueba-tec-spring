package com.forex.api.config;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration properties for Frankfurter API integration.
 * 
 * This class uses Spring Boot's @ConfigurationProperties to bind external
 * configuration from application.properties or environment variables.
 * Provides type-safe configuration with sensible defaults.
 * 
 * @author Forex API Team
 * @version 1.0
 * @since 2026-02-02
 */
@Configuration
@ConfigurationProperties(prefix = "frankfurter")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FrankfurterApiConfig {
    
    /**
     * Base URL for Frankfurter API.
     * Can be overridden via frankfurter.base-url property or FRANKFURTER_BASE_URL environment variable.
     */
    @Builder.Default
    private String baseUrl = FrankfurterApiConstants.DEFAULT_BASE_URL;
    
    /**
     * Connection timeout in milliseconds for HTTP requests to Frankfurter API.
     * Can be overridden via frankfurter.connect-timeout property.
     */
    @Builder.Default
    private int connectTimeout = FrankfurterApiConstants.DEFAULT_CONNECT_TIMEOUT_MS;
    
    /**
     * Read timeout in milliseconds for HTTP requests to Frankfurter API.
     * Can be overridden via frankfurter.read-timeout property.
     */
    @Builder.Default
    private int readTimeout = FrankfurterApiConstants.DEFAULT_READ_TIMEOUT_MS;
}
