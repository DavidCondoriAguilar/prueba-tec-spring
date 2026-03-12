package com.forex.api.validation;

import com.forex.api.dto.request.CurrencyConversionRequest;
import com.forex.api.exception.ForexApiException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Component
@Slf4j
public class ForexValidator {
    
    private static final List<String> VALID_CURRENCIES = Arrays.asList(
        "AED", "AFN", "ALL", "AMD", "ANG", "AOA", "ARS", "AUD", "AWG", "AZN",
        "BAM", "BBD", "BDT", "BGN", "BHD", "BIF", "BMD", "BND", "BOB", "BRL",
        "BSD", "BTN", "BWP", "BYN", "BZD", "CAD", "CDF", "CHF", "CLP", "CNY",
        "COP", "CRC", "CUC", "CUP", "CVE", "CZK", "DJF", "DKK", "DOP", "DZD",
        "EGP", "ERN", "ETB", "EUR", "FJD", "FKP", "GBP", "GEL", "GGP", "GHS",
        "GIP", "GMD", "GNF", "GTQ", "GYD", "HKD", "HNL", "HRK", "HTG", "HUF",
        "IDR", "ILS", "IMP", "INR", "IQD", "IRR", "ISK", "JEP", "JMD", "JOD",
        "JPY", "KES", "KGS", "KHR", "KMF", "KPW", "KRW", "KWD", "KYD", "KZT",
        "LAK", "LBP", "LKR", "LRD", "LSL", "LYD", "MAD", "MDL", "MGA", "MKD",
        "MMK", "MNT", "MOP", "MRU", "MUR", "MVR", "MWK", "MXN", "MYR", "MZN",
        "NAD", "NGN", "NIO", "NOK", "NPR", "NZD", "OMR", "PAB", "PEN", "PGK",
        "PHP", "PKR", "PLN", "PYG", "QAR", "RON", "RSD", "RUB", "RWF", "SAR",
        "SBD", "SCR", "SDG", "SEK", "SGD", "SHP", "SLL", "SOS", "SRD", "SSP",
        "STN", "SVC", "SYP", "SZL", "THB", "TJS", "TMT", "TND", "TOP", "TRY",
        "TTD", "TVD", "TWD", "TZS", "UAH", "UGX", "USD", "UYU", "UZS", "VES",
        "VND", "VUV", "WST", "XAF", "XCD", "XOF", "XPF", "YER", "ZAR", "ZMW",
        "ZWL"
    );
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    
    private static final Predicate<String> IS_VALID_CURRENCY = currency ->
        Optional.ofNullable(currency)
            .map(String::trim)
            .filter(c -> !c.isEmpty())
            .map(String::toUpperCase)
            .filter(VALID_CURRENCIES::contains)
            .isPresent();
    
    private static final Predicate<String> IS_VALID_DATE_FORMAT = date ->
        Optional.ofNullable(date)
            .map(String::trim)
            .filter(d -> !d.isEmpty())
            .isPresent();
    
    public void validateCurrencyCode(String currency) {
        log.debug("Validating currency code: {}", currency);
        
        Optional.ofNullable(currency)
            .map(String::trim)
            .map(String::toUpperCase)
            .filter(IS_VALID_CURRENCY)
            .orElseThrow(() -> {
                log.warn("Invalid currency code: {}", currency);
                return new ForexApiException("Código de divisa inválido: " + currency + ". Debe ser un código ISO 4217 válido.", 400);
            });
        
        log.debug("Currency code validation passed: {}", currency);
    }
    
    public void validateCurrencySymbols(String symbols) {
        log.debug("Validating currency symbols: {}", symbols);
        
        Optional.ofNullable(symbols)
            .map(String::trim)
            .filter(s -> !s.isEmpty())
            .map(s -> s.split(","))
            .map(Arrays::stream)
            .map(stream -> stream.map(String::trim).toList())
            .map(list -> {
                List<String> invalidCurrencies = list.stream()
                    .filter(symbol -> !IS_VALID_CURRENCY.test(symbol))
                    .collect(Collectors.toList());
                
                if (!invalidCurrencies.isEmpty()) {
                    throw new ForexApiException("Códigos de divisa inválidos: " + invalidCurrencies, 400);
                }
                return list;
            })
            .orElseThrow(() -> {
                log.warn("Empty symbols provided");
                throw new ForexApiException("Los símbolos no pueden estar vacíos", 400);
            });
        
        log.debug("Currency symbols validation passed: {}", symbols);
    }
    
    public void validateDate(String date) {
        log.debug("Validating date: {}", date);
        
        Optional.ofNullable(date)
            .map(String::trim)
            .filter(IS_VALID_DATE_FORMAT)
            .map(this::parseAndValidateDate)
            .orElseThrow(() -> {
                log.warn("Date is null or empty");
                throw new ForexApiException("El parámetro 'date' es requerido", 400);
            });
        
        log.debug("Date validation passed: {}", date);
    }
    
    private LocalDate parseAndValidateDate(String date) {
        try {
            LocalDate parsedDate = LocalDate.parse(date.trim(), DATE_FORMATTER);
            LocalDate today = LocalDate.now();
            LocalDate minDate = LocalDate.of(1999, 1, 4);
            
            if (parsedDate.isAfter(today)) {
                log.warn("Date is in the future: {}", date);
                throw new ForexApiException("La fecha no puede ser futura: " + date, 400);
            }
            
            if (parsedDate.isBefore(minDate)) {
                log.warn("Date is before minimum allowed date: {}", date);
                throw new ForexApiException("La fecha mínima permitida es 1999-01-04. Fecha proporcionada: " + date, 400);
            }
            
            log.debug("Date validation passed: {}", date);
            return parsedDate;
            
        } catch (DateTimeParseException e) {
            log.warn("Invalid date format: {}", date);
            throw new ForexApiException("Formato de fecha inválido. Use el formato YYYY-MM-DD. Fecha proporcionada: " + date, 400);
        }
    }
    
    public void validateConversionRequest(CurrencyConversionRequest request) {
        log.debug("Validating conversion request: {}", request);
        
        Optional.ofNullable(request)
            .orElseThrow(() -> {
                log.warn("Conversion request is null");
                throw new ForexApiException("El cuerpo de la solicitud es requerido", 400);
            });
        
        validateCurrencyCode(request.getFrom());
        validateCurrencyCode(request.getTo());
        
        if (request.getFrom().equalsIgnoreCase(request.getTo())) {
            log.warn("Source and target currencies are the same: {}", request.getFrom());
            throw new ForexApiException("Las divisas de origen y destino deben ser diferentes", 400);
        }
        
        Optional.ofNullable(request.getAmount())
            .filter(amount -> amount.compareTo(java.math.BigDecimal.ZERO) > 0)
            .orElseThrow(() -> {
                log.warn("Amount is not positive: {}", request.getAmount());
                throw new ForexApiException("El monto debe ser mayor a 0", 400);
            });
        
        log.debug("Conversion request validation passed: {}", request);
    }
    
    public boolean isValidCurrency(String currency) {
        return IS_VALID_CURRENCY.test(currency);
    }
}
