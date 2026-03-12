package com.history.api.controller;

import com.history.api.dto.ConversionRecord;
import com.history.api.service.HistoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

/**
 * REST Controller (WebFlux) - Endpoints para historial de conversiones.
 * Usa Mono/Flux para programación reactiva.
 */
@RestController
@RequestMapping("/history")
@RequiredArgsConstructor
@Slf4j
public class HistoryController {
    
    private final HistoryService historyService;
    
    /**
     * POST /history - Guarda una conversión.
     * @param record Datos de la conversión
     */
    @PostMapping
    public Mono<ResponseEntity<ConversionRecord>> saveConversion(@RequestBody ConversionRecord record) {
        return historyService.saveConversion(record)
            .map(saved -> ResponseEntity.ok(saved))
            .defaultIfEmpty(ResponseEntity.badRequest().build());
    }
    
    /**
     * GET /history/user/{userId} - Historial de un usuario.
     * @param userId ID del usuario
     */
    @GetMapping("/user/{userId}")
    public Flux<ConversionRecord> getHistoryByUser(@PathVariable String userId) {
        return historyService.getHistoryByUser(userId);
    }
    
    /**
     * GET /history/{id} - Obtiene una conversión por ID.
     * @param id ID de la conversión
     */
    @GetMapping("/{id}")
    public Mono<ResponseEntity<ConversionRecord>> getConversionById(@PathVariable Long id) {
        return historyService.getConversionById(id)
            .map(ResponseEntity::ok)
            .defaultIfEmpty(ResponseEntity.notFound().build());
    }
    
    /**
     * GET /history - Obtiene todo el historial.
     */
    @GetMapping
    public Flux<ConversionRecord> getAllHistory() {
        return historyService.getAllHistory();
    }
    
    /**
     * GET /history/between?start=...&end=... - Historial entre fechas.
     * @param start Fecha inicio (ISO format)
     * @param end Fecha fin
     */
    @GetMapping("/between")
    public ResponseEntity<?> getConversionsBetweenDates(
            @RequestParam String start,
            @RequestParam String end) {
        try {
            LocalDateTime startDate = LocalDateTime.parse(start);
            LocalDateTime endDate = LocalDateTime.parse(end);
            return ResponseEntity.ok(historyService.getConversionsBetweenDates(startDate, endDate));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Formato de fecha inválido. Use ISO format.");
        }
    }
}
