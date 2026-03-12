package com.forex.api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;
import java.time.Duration;

@Configuration
public class RestTemplateConfig {
    
    @Bean
    public RestTemplate restTemplate(FrankfurterApiConfig config) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout((int) Duration.ofMillis(config.getConnectTimeout()).toMillis());
        factory.setReadTimeout((int) Duration.ofMillis(config.getReadTimeout()).toMillis());
        
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.setRequestFactory(factory);
        return restTemplate;
    }
}
