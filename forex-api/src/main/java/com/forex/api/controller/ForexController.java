package com.forex.api.controller;

import com.forex.api.dto.request.CurrencyConversionRequest;
import com.forex.api.dto.request.HistoricalRatesRequest;
import com.forex.api.dto.request.LatestRatesRequest;
import com.forex.api.dto.response.CurrencyConversionResponse;
import com.forex.api.dto.response.ForexRatesResponse;
import com.forex.api.service.ForexService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for Forex API endpoints.
 * 
 * This class provides HTTP endpoints for forex rate operations including
 * latest rates, historical rates, and currency conversion. Implements
 * RESTful principles with proper HTTP methods, status codes, and error handling.
 * 
 * @author Forex API Team
 * @version 1.0
 * @since 2026-02-02
 */
@RestController
@RequestMapping("/forex")
@RequiredArgsConstructor
@Slf4j
public class ForexController {
    
    /**
     * Service layer for business logic operations.
     * Handles validation, data transformation, and external API communication.
     */
    private final ForexService forexService;
    
    /**
     * Retrieves latest forex rates based on query parameters.
     * 
     * HTTP Method: GET
     * Endpoint: /forex/latest
     * 
     * @param base Base currency code (optional, defaults to EUR if not provided)
     * @param symbols Comma-separated target currency symbols (optional, defaults to all currencies)
     * @return ResponseEntity containing ForexRatesResponse with current rates
     */
    @GetMapping("/latest")
    public ResponseEntity<ForexRatesResponse> getLatestRates(
            @RequestParam(defaultValue = "EUR") String base,
            @RequestParam(required = false) String symbols) {
        
        ForexRatesResponse response = forexService.getLatestRates(base, symbols);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Retrieves historical forex rates for a specific date.
     * 
     * HTTP Method: GET
     * Endpoint: /forex/historical
     * 
     * @param date Target date in YYYY-MM-DD format (required)
     * @param base Base currency code (optional, defaults to EUR if not provided)
     * @param symbols Comma-separated target currency symbols (optional, defaults to all currencies)
     * @return ResponseEntity containing ForexRatesResponse with historical rates
     */
    @GetMapping("/historical")
    public ResponseEntity<ForexRatesResponse> getHistoricalRates(
            @RequestParam String date,
            @RequestParam(defaultValue = "EUR") String base,
            @RequestParam(required = false) String symbols) {
        
        ForexRatesResponse response = forexService.getHistoricalRates(date, base, symbols);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Performs currency conversion between two currencies.
     * 
     * HTTP Method: POST
     * Endpoint: /forex/convert
     * 
     * @param request Request body containing conversion parameters
     * @return ResponseEntity containing CurrencyConversionResponse with conversion result
     */
    @PostMapping("/convert")
    public ResponseEntity<CurrencyConversionResponse> convertCurrency(@Valid @RequestBody CurrencyConversionRequest request) {
        
        CurrencyConversionResponse response = forexService.convertCurrency(request);
        return ResponseEntity.ok(response);
    }
}
