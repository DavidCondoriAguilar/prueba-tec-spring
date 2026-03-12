package com.history.api.service;

import com.history.api.dto.ConversionRecord;
import com.history.api.entity.ConversionHistory;
import com.history.api.repository.ConversionHistoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("HistoryService Tests")
class HistoryServiceTest {

    @Mock
    private ConversionHistoryRepository repository;

    @InjectMocks
    private HistoryService historyService;

    private ConversionHistory sampleEntity;
    private ConversionRecord sampleRecord;

    @BeforeEach
    void setUp() {
        sampleEntity = ConversionHistory.builder()
                .id(1L)
                .fromCurrency("EUR")
                .toCurrency("USD")
                .amount(BigDecimal.valueOf(100))
                .result(BigDecimal.valueOf(115.47))
                .rate(BigDecimal.valueOf(1.1547))
                .conversionDate(LocalDateTime.of(2026, 3, 12, 10, 30))
                .userId("user123")
                .build();

        sampleRecord = new ConversionRecord(
                1L,
                "EUR",
                "USD",
                BigDecimal.valueOf(100),
                BigDecimal.valueOf(115.47),
                BigDecimal.valueOf(1.1547),
                LocalDateTime.of(2026, 3, 12, 10, 30),
                "user123"
        );
    }

    @Nested
    @DisplayName("saveConversion Tests")
    class SaveConversionTest {

        @Test
        @DisplayName("Should save conversion successfully")
        void shouldSaveConversion_Successfully() {
            when(repository.save(any(ConversionHistory.class))).thenReturn(sampleEntity);

            Mono<ConversionRecord> result = historyService.saveConversion(sampleRecord);

            StepVerifier.create(result)
                    .assertNext(record -> {
                        assertThat(record.fromCurrency()).isEqualTo("EUR");
                        assertThat(record.toCurrency()).isEqualTo("USD");
                        assertThat(record.amount()).isEqualTo(BigDecimal.valueOf(100));
                    })
                    .verifyComplete();

            verify(repository).save(any(ConversionHistory.class));
        }

        @Test
        @DisplayName("Should save conversion with null date and user")
        void shouldSaveConversion_WithNullDateAndUser() {
            ConversionRecord recordWithNulls = new ConversionRecord(
                    null, "EUR", "USD", BigDecimal.valueOf(50),
                    BigDecimal.valueOf(57.73), BigDecimal.valueOf(1.1547),
                    null, null
            );

            ConversionHistory savedEntity = ConversionHistory.builder()
                    .id(2L)
                    .fromCurrency("EUR")
                    .toCurrency("USD")
                    .amount(BigDecimal.valueOf(50))
                    .result(BigDecimal.valueOf(57.73))
                    .rate(BigDecimal.valueOf(1.1547))
                    .conversionDate(LocalDateTime.now())
                    .userId("anonymous")
                    .build();

            when(repository.save(any(ConversionHistory.class))).thenReturn(savedEntity);

            Mono<ConversionRecord> result = historyService.saveConversion(recordWithNulls);

            StepVerifier.create(result)
                    .assertNext(record -> {
                        assertThat(record.userId()).isEqualTo("anonymous");
                        assertThat(record.conversionDate()).isNotNull();
                    })
                    .verifyComplete();
        }

        @Test
        @DisplayName("Should return empty when validation fails")
        void shouldReturnEmpty_WhenValidationFails() {
            ConversionRecord invalidRecord = new ConversionRecord(
                    null, null, null, null, null, null, null, null
            );

            Mono<ConversionRecord> result = historyService.saveConversion(invalidRecord);

            StepVerifier.create(result)
                    .verifyComplete();
        }
    }

    @Nested
    @DisplayName("getHistoryByUser Tests")
    class GetHistoryByUserTest {

        @Test
        @DisplayName("Should return history for user")
        void shouldReturnHistory_ForUser() {
            List<ConversionHistory> historyList = List.of(sampleEntity);
            when(repository.findByUserIdOrderByConversionDateDesc("user123")).thenReturn(historyList);

            Flux<ConversionRecord> result = historyService.getHistoryByUser("user123");

            StepVerifier.create(result)
                    .assertNext(record -> {
                        assertThat(record.userId()).isEqualTo("user123");
                        assertThat(record.fromCurrency()).isEqualTo("EUR");
                    })
                    .verifyComplete();
        }

        @Test
        @DisplayName("Should return empty when user has no history")
        void shouldReturnEmpty_WhenUserHasNoHistory() {
            when(repository.findByUserIdOrderByConversionDateDesc("nonexistent")).thenReturn(List.of());

            Flux<ConversionRecord> result = historyService.getHistoryByUser("nonexistent");

            StepVerifier.create(result)
                    .verifyComplete();
        }
    }

    @Nested
    @DisplayName("getConversionById Tests")
    class GetConversionByIdTest {

        @Test
        @DisplayName("Should return conversion by ID")
        void shouldReturnConversion_ById() {
            when(repository.findById(1L)).thenReturn(Optional.of(sampleEntity));

            Mono<ConversionRecord> result = historyService.getConversionById(1L);

            StepVerifier.create(result)
                    .assertNext(record -> {
                        assertThat(record.id()).isEqualTo(1L);
                        assertThat(record.fromCurrency()).isEqualTo("EUR");
                    })
                    .verifyComplete();
        }

        @Test
        @DisplayName("Should return empty when ID not found")
        void shouldReturnEmpty_WhenIdNotFound() {
            when(repository.findById(999L)).thenReturn(Optional.empty());

            Mono<ConversionRecord> result = historyService.getConversionById(999L);

            StepVerifier.create(result)
                    .verifyComplete();
        }
    }

    @Nested
    @DisplayName("getConversionsBetweenDates Tests")
    class GetConversionsBetweenDatesTest {

        @Test
        @DisplayName("Should return conversions between dates")
        void shouldReturnConversions_BetweenDates() {
            LocalDateTime start = LocalDateTime.of(2026, 1, 1, 0, 0);
            LocalDateTime end = LocalDateTime.of(2026, 12, 31, 23, 59);

            List<ConversionHistory> historyList = List.of(sampleEntity);
            when(repository.findByConversionDateBetween(start, end)).thenReturn(historyList);

            List<ConversionRecord> result = historyService.getConversionsBetweenDates(start, end);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).fromCurrency()).isEqualTo("EUR");
        }

        @Test
        @DisplayName("Should return empty list when no conversions in range")
        void shouldReturnEmptyList_WhenNoConversionsInRange() {
            LocalDateTime start = LocalDateTime.of(2025, 1, 1, 0, 0);
            LocalDateTime end = LocalDateTime.of(2025, 12, 31, 23, 59);

            when(repository.findByConversionDateBetween(start, end)).thenReturn(List.of());

            List<ConversionRecord> result = historyService.getConversionsBetweenDates(start, end);

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("getAllHistory Tests")
    class GetAllHistoryTest {

        @Test
        @DisplayName("Should return all history")
        @SuppressWarnings("unchecked")
        void shouldReturnAllHistory() {
            List<ConversionHistory> historyList = List.of(sampleEntity);
            when(repository.findAll()).thenReturn(historyList);

            Flux<ConversionRecord> result = historyService.getAllHistory();

            StepVerifier.create(result)
                    .assertNext(record -> assertThat(record.id()).isEqualTo(1L))
                    .verifyComplete();
        }

        @Test
        @DisplayName("Should return empty when no history")
        @SuppressWarnings("unchecked")
        void shouldReturnEmpty_WhenNoHistory() {
            when(repository.findAll()).thenReturn(List.of());

            Flux<ConversionRecord> result = historyService.getAllHistory();

            StepVerifier.create(result)
                    .verifyComplete();
        }
    }
}
