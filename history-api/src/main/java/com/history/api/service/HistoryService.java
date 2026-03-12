package com.history.api.service;

import com.history.api.dto.ConversionRecord;
import com.history.api.entity.ConversionHistory;
import com.history.api.repository.ConversionHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Service - Lógica de negocio para historial de conversiones.
 * Usa programación reactiva (Mono/Flux) con JPA blocking.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class HistoryService {
    
    private final ConversionHistoryRepository repository;
    
    // Predicate para validar historial antes de guardar
    private static final Predicate<ConversionHistory> IS_VALID_HISTORY = history ->
        Optional.ofNullable(history)
            .filter(h -> h.getFromCurrency() != null && !h.getFromCurrency().isEmpty())
            .filter(h -> h.getToCurrency() != null && !h.getToCurrency().isEmpty())
            .filter(h -> h.getAmount() != null && h.getAmount().compareTo(java.math.BigDecimal.ZERO) > 0)
            .isPresent();
    
    /**
     * Guarda una conversión en la base de datos.
     * @param record Datos de la conversión
     * @return ConversionRecord guardado o vacío si falla
     */
    public Mono<ConversionRecord> saveConversion(ConversionRecord record) {
        ConversionHistory history = toEntity(record);
        
        return Mono.fromCallable(() -> history)
            .filter(IS_VALID_HISTORY)
            .map(repository::save)
            .map(this::toRecord)
            .onErrorResume(e -> Mono.empty());
    }
    
    /**
     * Obtiene historial de un usuario ordenado por fecha descendente.
     * @param userId ID del usuario
     */
    public Flux<ConversionRecord> getHistoryByUser(String userId) {
        List<ConversionHistory> list = repository.findByUserIdOrderByConversionDateDesc(userId);
        return Flux.fromIterable(list).map(this::toRecord);
    }
    
    /**
     * Obtiene una conversión por su ID.
     * @param id ID de la conversión
     */
    public Mono<ConversionRecord> getConversionById(Long id) {
        return Mono.justOrEmpty(repository.findById(id)).map(this::toRecord);
    }
    
    /**
     * Obtiene conversiones entre dos fechas.
     * @param start Fecha inicio
     * @param end Fecha fin
     */
    public List<ConversionRecord> getConversionsBetweenDates(LocalDateTime start, LocalDateTime end) {
        return repository.findByConversionDateBetween(start, end).stream()
            .map(this::toRecord)
            .collect(Collectors.toList());
    }
    
    /**
     * Obtiene todo el historial de conversiones.
     */
    public Flux<ConversionRecord> getAllHistory() {
        return Flux.fromIterable(repository.findAll()).map(this::toRecord);
    }
    
    // Mapper: ConversionRecord → ConversionHistory
    private ConversionHistory toEntity(ConversionRecord record) {
        return ConversionHistory.builder()
            .fromCurrency(record.fromCurrency())
            .toCurrency(record.toCurrency())
            .amount(record.amount())
            .result(record.result())
            .rate(record.rate())
            .conversionDate(Optional.ofNullable(record.conversionDate()).orElse(LocalDateTime.now()))
            .userId(Optional.ofNullable(record.userId()).orElse("anonymous"))
            .build();
    }
    
    // Mapper: ConversionHistory → ConversionRecord
    private ConversionRecord toRecord(ConversionHistory entity) {
        return new ConversionRecord(
            entity.getId(),
            entity.getFromCurrency(),
            entity.getToCurrency(),
            entity.getAmount(),
            entity.getResult(),
            entity.getRate(),
            entity.getConversionDate(),
            entity.getUserId()
        );
    }
}
