package com.history.api.controller;

import com.history.api.dto.ConversionRecord;
import com.history.api.service.HistoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("HistoryController Tests")
class HistoryControllerTest {

    @Mock
    private HistoryService historyService;

    @InjectMocks
    private HistoryController historyController;

    private ConversionRecord sampleRecord;

    @BeforeEach
    void setUp() {
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
    @DisplayName("POST /history Tests")
    class SaveConversionTest {

        @Test
        @DisplayName("Should save conversion successfully")
        void shouldSaveConversion_Successfully() {
            when(historyService.saveConversion(sampleRecord))
                    .thenReturn(Mono.just(sampleRecord));

            Mono<ResponseEntity<ConversionRecord>> result = 
                    historyController.saveConversion(sampleRecord);

            StepVerifier.create(result)
                    .assertNext(response -> {
                        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                        assertThat(response.getBody()).isNotNull();
                        assertThat(response.getBody().fromCurrency()).isEqualTo("EUR");
                    })
                    .verifyComplete();
        }

        @Test
        @DisplayName("Should return bad request when save fails")
        void shouldReturnBadRequest_WhenSaveFails() {
            when(historyService.saveConversion(sampleRecord))
                    .thenReturn(Mono.empty());

            Mono<ResponseEntity<ConversionRecord>> result = 
                    historyController.saveConversion(sampleRecord);

            StepVerifier.create(result)
                    .assertNext(response -> 
                        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST))
                    .verifyComplete();
        }
    }

    @Nested
    @DisplayName("GET /history/user/{userId} Tests")
    class GetHistoryByUserTest {

        @Test
        @DisplayName("Should return history for user")
        void shouldReturnHistory_ForUser() {
            List<ConversionRecord> records = List.of(sampleRecord);
            when(historyService.getHistoryByUser("user123"))
                    .thenReturn(Flux.fromIterable(records));

            Flux<ConversionRecord> result = historyController.getHistoryByUser("user123");

            StepVerifier.create(result)
                    .assertNext(record -> assertThat(record.userId()).isEqualTo("user123"))
                    .verifyComplete();
        }

        @Test
        @DisplayName("Should return empty when user has no history")
        void shouldReturnEmpty_WhenUserHasNoHistory() {
            when(historyService.getHistoryByUser("nonexistent"))
                    .thenReturn(Flux.empty());

            Flux<ConversionRecord> result = historyController.getHistoryByUser("nonexistent");

            StepVerifier.create(result)
                    .verifyComplete();
        }
    }

    @Nested
    @DisplayName("GET /history/{id} Tests")
    class GetConversionByIdTest {

        @Test
        @DisplayName("Should return conversion by ID")
        void shouldReturnConversion_ById() {
            when(historyService.getConversionById(1L))
                    .thenReturn(Mono.just(sampleRecord));

            Mono<ResponseEntity<ConversionRecord>> result = 
                    historyController.getConversionById(1L);

            StepVerifier.create(result)
                    .assertNext(response -> {
                        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                        assertThat(response.getBody().id()).isEqualTo(1L);
                    })
                    .verifyComplete();
        }

        @Test
        @DisplayName("Should return not found when ID does not exist")
        void shouldReturnNotFound_WhenIdNotFound() {
            when(historyService.getConversionById(999L))
                    .thenReturn(Mono.empty());

            Mono<ResponseEntity<ConversionRecord>> result = 
                    historyController.getConversionById(999L);

            StepVerifier.create(result)
                    .assertNext(response -> 
                        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND))
                    .verifyComplete();
        }
    }

    @Nested
    @DisplayName("GET /history Tests")
    class GetAllHistoryTest {

        @Test
        @DisplayName("Should return all history")
        void shouldReturnAllHistory() {
            List<ConversionRecord> records = List.of(sampleRecord);
            when(historyService.getAllHistory())
                    .thenReturn(Flux.fromIterable(records));

            Flux<ConversionRecord> result = historyController.getAllHistory();

            StepVerifier.create(result)
                    .assertNext(record -> assertThat(record.id()).isEqualTo(1L))
                    .verifyComplete();
        }
    }

    @Nested
    @DisplayName("GET /history/between Tests")
    class GetConversionsBetweenDatesTest {

        @Test
        @DisplayName("Should return conversions between dates")
        void shouldReturnConversions_BetweenDates() {
            List<ConversionRecord> records = List.of(sampleRecord);
            when(historyService.getConversionsBetweenDates(
                    LocalDateTime.parse("2026-01-01T00:00:00"),
                    LocalDateTime.parse("2026-12-31T23:59:59")))
                    .thenReturn(records);

            ResponseEntity<?> result = historyController.getConversionsBetweenDates(
                    "2026-01-01T00:00:00",
                    "2026-12-31T23:59:59");

            assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(result.getBody()).isNotNull();
        }

        @Test
        @DisplayName("Should return bad request for invalid date format")
        void shouldReturnBadRequest_ForInvalidDateFormat() {
            ResponseEntity<?> result = historyController.getConversionsBetweenDates(
                    "invalid-date",
                    "also-invalid");

            assertThat(result.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(result.getBody()).isEqualTo("Formato de fecha inválido. Use ISO format.");
        }
    }
}
