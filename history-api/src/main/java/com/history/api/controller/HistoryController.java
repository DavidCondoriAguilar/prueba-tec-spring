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

@RestController
@RequestMapping("/history")
@RequiredArgsConstructor
@Slf4j
public class HistoryController {
    
    private final HistoryService historyService;
    
    @PostMapping
    public Mono<ResponseEntity<ConversionRecord>> saveConversion(@RequestBody ConversionRecord record) {
        log.info("Recibida solicitud para guardar conversión: {}", record);
        
        return historyService.saveConversion(record)
            .map(saved -> ResponseEntity.ok(saved))
            .defaultIfEmpty(ResponseEntity.badRequest().build());
    }
    
    @GetMapping("/user/{userId}")
    public Flux<ConversionRecord> getHistoryByUser(@PathVariable String userId) {
        log.info("Obteniendo historial para usuario: {}", userId);
        
        return historyService.getHistoryByUser(userId);
    }
    
    @GetMapping("/{id}")
    public Mono<ResponseEntity<ConversionRecord>> getConversionById(@PathVariable Long id) {
        log.info("Obteniendo conversión con ID: {}", id);
        
        return historyService.getConversionById(id)
            .map(ResponseEntity::ok)
            .defaultIfEmpty(ResponseEntity.notFound().build());
    }
    
    @GetMapping
    public Flux<ConversionRecord> getAllHistory() {
        log.info("Obteniendo todo el historial");
        
        return historyService.getAllHistory();
    }
    
    @GetMapping("/between")
    public ResponseEntity<?> getConversionsBetweenDates(
            @RequestParam String start,
            @RequestParam String end) {
        
        log.info("Obteniendo conversiones entre {} y {}", start, end);
        
        try {
            LocalDateTime startDate = LocalDateTime.parse(start);
            LocalDateTime endDate = LocalDateTime.parse(end);
            
            var result = historyService.getConversionsBetweenDates(startDate, endDate);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Error al obtener conversiones entre fechas: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Formato de fecha inválido. Use ISO format.");
        }
    }
}
