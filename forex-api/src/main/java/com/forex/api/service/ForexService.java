package com.forex.api.service;

import com.forex.api.dto.FrankfurterResponse;
import com.forex.api.dto.request.CurrencyConversionRequest;
import com.forex.api.dto.response.CurrencyConversionResponse;
import com.forex.api.dto.response.ForexRatesResponse;
import com.forex.api.entity.ForexRate;
import com.forex.api.exception.ForexApiException;
import com.forex.api.mapper.ForexMapper;
import com.forex.api.repository.FrankfurterRepository;
import com.forex.api.validation.ForexValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Service - Lógica de negocio para operaciones de forex.
 * Valida, obtiene tasas de API externa y calcula conversiones.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ForexService {
    
    private final FrankfurterRepository repository;
    private final ForexMapper mapper;
    private final ForexValidator validator;
    
    // Exceptions factory
    private static final Supplier<ForexApiException> VALIDATION_ERROR = () ->
        new ForexApiException("Error de validación en los parámetros proporcionados", 400);
    
    private static final Supplier<ForexApiException> CONVERSION_ERROR = () ->
        new ForexApiException("No se encontró tasa de cambio para la conversión solicitada", 404);
    
    // Predicate para validar moneda (inmutable, reutilizable)
    private static final Predicate<String> IS_VALID_CURRENCY = currency ->
        Optional.ofNullable(currency)
            .map(String::trim)
            .filter(c -> !c.isEmpty())
            .isPresent();
    
    /**
     * Obtiene tasas de cambio actuales.
     * @param base Moneda base (EUR por defecto)
     * @param symbols Monedas objetivo (opcional)
     */
    public ForexRatesResponse getLatestRates(String base, String symbols) {
        return Optional.ofNullable(base)
            .filter(IS_VALID_CURRENCY)
            .map(validBase -> executeLatestRatesFetch(validBase, symbols))
            .orElseThrow(VALIDATION_ERROR);
    }
    
    private ForexRatesResponse executeLatestRatesFetch(String base, String symbols) {
        Optional.ofNullable(symbols)
            .filter(s -> !s.trim().isEmpty())
            .ifPresent(validator::validateCurrencySymbols);
        
        FrankfurterResponse response = repository.getLatestRates(base, symbols);
        
        return Optional.ofNullable(response)
            .map(mapper::toForexRate)
            .map(mapper::toForexRatesResponse)
            .orElseThrow(() -> new ForexApiException("No se pudieron obtener las tasas de cambio", 404));
    }
    
    /**
     * Obtiene tasas históricas para una fecha específica.
     * @param date Fecha en formato YYYY-MM-DD
     * @param base Moneda base
     * @param symbols Monedas objetivo
     */
    public ForexRatesResponse getHistoricalRates(String date, String base, String symbols) {
        return Optional.ofNullable(date)
            .filter(IS_VALID_CURRENCY)
            .map(validDate -> executeHistoricalRatesFetch(validDate, base, symbols))
            .orElseThrow(VALIDATION_ERROR);
    }
    
    private ForexRatesResponse executeHistoricalRatesFetch(String date, String base, String symbols) {
        validator.validateDate(date);
        
        Optional.ofNullable(base)
            .filter(IS_VALID_CURRENCY)
            .ifPresent(validator::validateCurrencyCode);
        
        Optional.ofNullable(symbols)
            .filter(s -> !s.trim().isEmpty())
            .ifPresent(validator::validateCurrencySymbols);
        
        String effectiveBase = Optional.ofNullable(base).orElse("EUR");
        
        FrankfurterResponse response = repository.getHistoricalRates(date, effectiveBase, symbols);
        
        return Optional.ofNullable(response)
            .map(mapper::toForexRate)
            .map(mapper::toForexRatesResponse)
            .orElseThrow(() -> new ForexApiException("No se encontraron datos históricos para: " + date, 404));
    }
    
    /**
     * Convierte moneda: obtiene tasa, calcula resultado y guarda en historial.
     * @param request {from, to, amount, userId}
     * @return {from, to, amount, rate, result, date}
     */
    public CurrencyConversionResponse convertCurrency(CurrencyConversionRequest request) {
        return Optional.ofNullable(request)
            .filter(this::isValidConversionRequest)
            .map(this::performConversion)
            .orElseThrow(VALIDATION_ERROR);
    }
    
    private boolean isValidConversionRequest(CurrencyConversionRequest request) {
        if (request == null) return false;
        if (request.getFrom() == null || request.getTo() == null || request.getAmount() == null) return false;
        if (request.getFrom().equalsIgnoreCase(request.getTo())) return false;
        return request.getAmount().compareTo(BigDecimal.ZERO) > 0;
    }
    
    private CurrencyConversionResponse performConversion(CurrencyConversionRequest request) {
        FrankfurterResponse response = repository.getLatestRates(request.getFrom(), request.getTo());
        return extractConversionResult(response, request);
    }
    
    private CurrencyConversionResponse extractConversionResult(FrankfurterResponse response, 
                                                               CurrencyConversionRequest request) {
        return Optional.ofNullable(response.getRates())
            .filter(rates -> rates.containsKey(request.getTo()))
            .map(rates -> {
                BigDecimal rate = BigDecimal.valueOf(rates.get(request.getTo()));
                BigDecimal result = request.getAmount().multiply(rate).setScale(2, RoundingMode.HALF_UP);
                
                return CurrencyConversionResponse.builder()
                    .from(request.getFrom())
                    .to(request.getTo())
                    .amount(request.getAmount())
                    .rate(rate)
                    .result(result)
                    .date(LocalDate.parse(response.getDate()))
                    .build();
            })
            .orElseThrow(CONVERSION_ERROR);
    }
    
    /**
     * Obtiene tasas con filtro personalizado (uso interno/demostración de Streams).
     */
    public Map<String, Double> getRatesWithFilter(String base, String symbols, 
                                                   java.util.function.Predicate<String> symbolFilter) {
        String effectiveBase = Optional.ofNullable(base).orElse("EUR");
        FrankfurterResponse response = repository.getLatestRates(effectiveBase, symbols);
        
        return Optional.ofNullable(response)
            .map(FrankfurterResponse::getRates)
            .map(rates -> rates.entrySet().stream()
                .filter(entry -> symbolFilter.test(entry.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)))
            .orElse(Map.of());
    }
}
