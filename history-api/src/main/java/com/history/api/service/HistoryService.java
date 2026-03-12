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

@Service
@RequiredArgsConstructor
@Slf4j
public class HistoryService {
    
    private final ConversionHistoryRepository repository;
    
    private static final Consumer<ConversionHistory> LOG_SAVE = history -> 
        log.info("Guardando conversión: {} -> {} por usuario: {}", 
            history.getFromCurrency(), history.getToCurrency(), history.getUserId());
    
    private static final Predicate<ConversionHistory> IS_VALID_HISTORY = history ->
        Optional.ofNullable(history)
            .filter(h -> h.getFromCurrency() != null && !h.getFromCurrency().isEmpty())
            .filter(h -> h.getToCurrency() != null && !h.getToCurrency().isEmpty())
            .filter(h -> h.getAmount() != null && h.getAmount().compareTo(java.math.BigDecimal.ZERO) > 0)
            .isPresent();
    
    public Mono<ConversionRecord> saveConversion(ConversionRecord record) {
        log.info("Guardando conversión: {} -> {} amount: {}", 
            record.fromCurrency(), record.toCurrency(), record.amount());
        
        ConversionHistory history = toEntity(record);
        
        return Mono.fromCallable(() -> history)
            .filter(h -> IS_VALID_HISTORY.test(h))
            .doOnNext(LOG_SAVE)
            .map(repository::save)
            .map(this::toRecord)
            .onErrorResume(e -> {
                log.error("Error al guardar conversión: {}", e.getMessage());
                return Mono.empty();
            });
    }
    
    public Flux<ConversionRecord> getHistoryByUser(String userId) {
        log.debug("Obteniendo historial para usuario: {}", userId);
        
        List<ConversionHistory> list = repository.findByUserIdOrderByConversionDateDesc(userId);
        
        return Flux.fromIterable(list)
            .map(this::toRecord)
            .doOnComplete(() -> log.debug("Historial obtenido para usuario: {}", userId));
    }
    
    public Mono<ConversionRecord> getConversionById(Long id) {
        log.debug("Obteniendo conversión con ID: {}", id);
        
        return Mono.justOrEmpty(repository.findById(id))
            .map(this::toRecord)
            .doOnSuccess(record -> log.debug("Conversión encontrada: {}", record));
    }
    
    public List<ConversionRecord> getConversionsBetweenDates(LocalDateTime start, LocalDateTime end) {
        log.debug("Obteniendo conversiones entre {} y {}", start, end);
        
        return repository.findByConversionDateBetween(start, end).stream()
            .map(this::toRecord)
            .collect(Collectors.toList());
    }
    
    public Flux<ConversionRecord> getAllHistory() {
        log.debug("Obteniendo todo el historial");
        
        return Flux.fromIterable(repository.findAll())
            .map(this::toRecord);
    }
    
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
