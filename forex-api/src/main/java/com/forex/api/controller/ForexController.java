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
 * REST Controller - Endpoints para tasas de cambio y conversión de divisas.
 * Expone API RESTful para operaciones de forex.
 */
@RestController
@RequestMapping("/forex")
@RequiredArgsConstructor
@Slf4j
public class ForexController {
    
    private final ForexService forexService;
    
    /**
     * GET /forex/latest - Obtiene tasas de cambio actuales.
     * @param base Moneda base (default: EUR)
     * @param symbols Monedas objetivo (opcional)
     */
    @GetMapping("/latest")
    public ResponseEntity<ForexRatesResponse> getLatestRates(
            @RequestParam(defaultValue = "EUR") String base,
            @RequestParam(required = false) String symbols) {
        return ResponseEntity.ok(forexService.getLatestRates(base, symbols));
    }
    
    /**
     * GET /forex/historical - Obtiene tasas históricas.
     * @param date Fecha en formato YYYY-MM-DD
     * @param base Moneda base (default: EUR)
     * @param symbols Monedas objetivo (opcional)
     */
    @GetMapping("/historical")
    public ResponseEntity<ForexRatesResponse> getHistoricalRates(
            @RequestParam String date,
            @RequestParam(defaultValue = "EUR") String base,
            @RequestParam(required = false) String symbols) {
        return ResponseEntity.ok(forexService.getHistoricalRates(date, base, symbols));
    }
    
    /**
     * POST /forex/convert - Convierte moneda de una a otra.
     * @param request {from, to, amount, userId}
     */
    @PostMapping("/convert")
    public ResponseEntity<CurrencyConversionResponse> convertCurrency(
            @Valid @RequestBody CurrencyConversionRequest request) {
        return ResponseEntity.ok(forexService.convertCurrency(request));
    }
}
