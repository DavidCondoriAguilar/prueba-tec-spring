package com.forex.api.repository;

import com.forex.api.config.FrankfurterApiConfig;
import com.forex.api.config.FrankfurterApiConstants;
import com.forex.api.dto.FrankfurterResponse;
import com.forex.api.exception.ForexApiException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.Map;
import java.util.function.Supplier;

@Repository
@RequiredArgsConstructor
@Slf4j
public class FrankfurterRepository {
    
    private final WebClient webClient;
    private final FrankfurterApiConfig config;
    
    private static final Supplier<ForexApiException> EXTERNAL_API_ERROR = () -> 
        new ForexApiException(FrankfurterApiConstants.ERROR_EXTERNAL_API_UNAVAILABLE, 
                              FrankfurterApiConstants.HTTP_SERVICE_UNAVAILABLE);
    
    private static final Supplier<ForexApiException> SERVER_ERROR = () -> 
        new ForexApiException(FrankfurterApiConstants.ERROR_EXTERNAL_API_SERVER_ERROR, 
                              FrankfurterApiConstants.HTTP_SERVICE_UNAVAILABLE);
    
    @Cacheable(value = FrankfurterApiConstants.LATEST_RATES_CACHE, key = "{#base, #symbols}")
    public FrankfurterResponse getLatestRates(String base, String symbols) {
        log.debug("Fetching latest rates for base: {}, symbols: {}", base, symbols);
        
        return Mono.defer(() -> buildLatestRatesRequest(base, symbols))
                .retryWhen(Retry.backoff(3, Duration.ofMillis(500))
                        .filter(throwable -> !(throwable instanceof WebClientResponseException.NotFound)))
                .doOnSuccess(response -> log.debug("Successfully fetched rates: {}", response))
                .doOnError(error -> log.error("Error fetching rates: {}", error.getMessage()))
                .onErrorResume(WebClientResponseException.NotFound.class, 
                    e -> Mono.error(new ForexApiException("No se encontraron datos: " + e.getMessage(), 404)))
                .onErrorResume(WebClientResponseException.class, 
                    e -> Mono.error(SERVER_ERROR.get()))
                .onErrorResume(Exception.class, 
                    e -> Mono.error(EXTERNAL_API_ERROR.get()))
                .block();
    }
    
    private Mono<FrankfurterResponse> buildLatestRatesRequest(String base, String symbols) {
        return webClient.get()
                .uri(uriBuilder -> {
                    var builder = uriBuilder
                            .path(FrankfurterApiConstants.LATEST_ENDPOINT)
                            .queryParam(FrankfurterApiConstants.BASE_PARAM, base);
                    
                    if (symbols != null && !symbols.trim().isEmpty()) {
                        builder.queryParam(FrankfurterApiConstants.SYMBOLS_PARAM, symbols);
                    }
                    
                    return builder.build();
                })
                .retrieve()
                .bodyToMono(FrankfurterResponse.class);
    }
    
    @Cacheable(value = FrankfurterApiConstants.HISTORICAL_RATES_CACHE, key = "{#date, #base, #symbols}")
    public FrankfurterResponse getHistoricalRates(String date, String base, String symbols) {
        log.debug("Fetching historical rates for date: {}, base: {}, symbols: {}", date, base, symbols);
        
        return Mono.defer(() -> buildHistoricalRatesRequest(date, base, symbols))
                .retryWhen(Retry.backoff(3, Duration.ofMillis(500))
                        .filter(throwable -> !(throwable instanceof WebClientResponseException.NotFound)))
                .doOnSuccess(response -> log.debug("Successfully fetched historical rates: {}", response))
                .doOnError(error -> log.error("Error fetching historical rates: {}", error.getMessage()))
                .onErrorResume(WebClientResponseException.NotFound.class, 
                    e -> Mono.error(new ForexApiException(
                        FrankfurterApiConstants.ERROR_NO_HISTORICAL_DATA + ": " + date, 
                        FrankfurterApiConstants.HTTP_NOT_FOUND)))
                .onErrorResume(WebClientResponseException.class, 
                    e -> Mono.error(SERVER_ERROR.get()))
                .onErrorResume(Exception.class, 
                    e -> Mono.error(EXTERNAL_API_ERROR.get()))
                .block();
    }
    
    private Mono<FrankfurterResponse> buildHistoricalRatesRequest(String date, String base, String symbols) {
        return webClient.get()
                .uri(uriBuilder -> {
                    var builder = uriBuilder
                            .path(String.format(FrankfurterApiConstants.HISTORICAL_ENDPOINT_FORMAT, date))
                            .queryParam(FrankfurterApiConstants.BASE_PARAM, base);
                    
                    if (symbols != null && !symbols.trim().isEmpty()) {
                        builder.queryParam(FrankfurterApiConstants.SYMBOLS_PARAM, symbols);
                    }
                    
                    return builder.build();
                })
                .retrieve()
                .bodyToMono(FrankfurterResponse.class);
    }
    
    public Map<String, Double> getAvailableCurrencies() {
        log.debug("Fetching available currencies");
        
        return webClient.get()
                .uri(FrankfurterApiConstants.CURRENCIES_ENDPOINT)
                .retrieve()
                .bodyToMono(new org.springframework.core.ParameterizedTypeReference<Map<String, Double>>() {})
                .retryWhen(Retry.backoff(3, Duration.ofMillis(500)))
                .block();
    }
}
