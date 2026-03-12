package com.forex.api.config;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración - Propiedades para API externa de Frankfurter.
 * Se bindea desde application.properties.
 */
@Configuration
@ConfigurationProperties(prefix = "frankfurter")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FrankfurterApiConfig {
    
    @Builder.Default
    private String baseUrl = FrankfurterApiConstants.DEFAULT_BASE_URL;
    
    @Builder.Default
    private int connectTimeout = FrankfurterApiConstants.DEFAULT_CONNECT_TIMEOUT_MS;
    
    @Builder.Default
    private int readTimeout = FrankfurterApiConstants.DEFAULT_READ_TIMEOUT_MS;
}
