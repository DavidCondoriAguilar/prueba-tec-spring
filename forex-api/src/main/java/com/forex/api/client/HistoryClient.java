package com.forex.api.client;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * Client HTTP - Comunica con History API (otro microservicio).
 * Usa WebClient reactivo para llamadas HTTP.
 */
@Component
public class HistoryClient {
    
    private final WebClient webClient;
    
    public HistoryClient() {
        this.webClient = WebClient.builder()
            .baseUrl("http://localhost:8081")
            .build();
    }
    
    /**
     * Guarda conversión en History API via HTTP POST.
     * @param from Moneda origen
     * @param to Moneda destino
     * @param amount Monto original
     * @param result Monto convertido
     * @param rate Tasa de cambio
     * @param userId ID del usuario
     */
    public Mono<Map> saveConversion(String from, String to, BigDecimal amount, 
                                    BigDecimal result, BigDecimal rate, String userId) {
        Map<String, Object> request = Map.of(
            "fromCurrency", from,
            "toCurrency", to,
            "amount", amount,
            "result", result,
            "rate", rate,
            "conversionDate", LocalDateTime.now().toString(),
            "userId", userId != null ? userId : "anonymous"
        );
        
        return webClient.post()
            .uri("/history")
            .bodyValue(request)
            .retrieve()
            .bodyToMono(Map.class);
    }
}
