package com.forex.api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Bean
    public WebClient webClient(FrankfurterApiConfig config) {
        return WebClient.builder()
                .baseUrl(config.getBaseUrl())
                .defaultHeader("Accept", "application/json")
                .build();
    }
}
