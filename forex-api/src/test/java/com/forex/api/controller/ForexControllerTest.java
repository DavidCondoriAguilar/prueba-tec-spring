package com.forex.api.controller;

import com.forex.api.dto.request.CurrencyConversionRequest;
import com.forex.api.dto.response.CurrencyConversionResponse;
import com.forex.api.dto.response.ForexRatesResponse;
import com.forex.api.exception.ForexApiException;
import com.forex.api.service.ForexService;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ForexController Tests")
class ForexControllerTest {

    @Mock
    private ForexService forexService;

    @InjectMocks
    private ForexController forexController;

    private ForexRatesResponse sampleRatesResponse;
    private CurrencyConversionResponse sampleConversionResponse;

    @BeforeEach
    void setUp() {
        sampleRatesResponse = ForexRatesResponse.builder()
                .base("EUR")
                .date(LocalDate.of(2026, 3, 12))
                .rates(Map.of("USD", 1.1547, "GBP", 0.86243))
                .build();

        sampleConversionResponse = CurrencyConversionResponse.builder()
                .from("EUR")
                .to("USD")
                .amount(BigDecimal.valueOf(100))
                .rate(BigDecimal.valueOf(1.1547))
                .result(BigDecimal.valueOf(115.47))
                .date(LocalDate.of(2026, 3, 12))
                .build();
    }

    @Nested
    @DisplayName("GET /forex/latest Tests")
    class GetLatestRatesTest {

        @Test
        @DisplayName("Should return latest rates with default base currency")
        void shouldReturnLatestRates_WithDefaultBaseCurrency() {
            when(forexService.getLatestRates("EUR", null)).thenReturn(sampleRatesResponse);

            ResponseEntity<ForexRatesResponse> response = forexController.getLatestRates("EUR", null);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getBase()).isEqualTo("EUR");
            assertThat(response.getBody().getRates()).containsKey("USD");
        }

        @Test
        @DisplayName("Should return latest rates with custom base currency")
        void shouldReturnLatestRates_WithCustomBaseCurrency() {
            when(forexService.getLatestRates("USD", "EUR,GBP")).thenReturn(sampleRatesResponse);

            ResponseEntity<ForexRatesResponse> response = forexController.getLatestRates("USD", "EUR,GBP");

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
        }

        @Test
        @DisplayName("Should return latest rates with specific symbols")
        void shouldReturnLatestRates_WithSpecificSymbols() {
            ForexRatesResponse filteredResponse = ForexRatesResponse.builder()
                    .base("EUR")
                    .date(LocalDate.of(2026, 3, 12))
                    .rates(Map.of("USD", 1.1547))
                    .build();

            when(forexService.getLatestRates("EUR", "USD")).thenReturn(filteredResponse);

            ResponseEntity<ForexRatesResponse> response = forexController.getLatestRates("EUR", "USD");

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody().getRates()).containsOnlyKeys("USD");
        }

        @Test
        @DisplayName("Should throw exception when service throws ForexApiException")
        void shouldThrowException_WhenServiceThrowsException() {
            when(forexService.getLatestRates(anyString(), any()))
                    .thenThrow(new ForexApiException("Error de validación", 400));

            assertThatThrownBy(() -> forexController.getLatestRates("INVALID", null))
                    .isInstanceOf(ForexApiException.class)
                    .hasMessageContaining("Error de validación");
        }
    }

    @Nested
    @DisplayName("GET /forex/historical Tests")
    class GetHistoricalRatesTest {

        @Test
        @DisplayName("Should return historical rates for valid date")
        void shouldReturnHistoricalRates_ForValidDate() {
            ForexRatesResponse historicalResponse = ForexRatesResponse.builder()
                    .base("EUR")
                    .date(LocalDate.of(2024, 1, 15))
                    .rates(Map.of("USD", 1.0945))
                    .build();
            
            when(forexService.getHistoricalRates("2024-01-15", "EUR", null))
                    .thenReturn(historicalResponse);

            ResponseEntity<ForexRatesResponse> response = 
                    forexController.getHistoricalRates("2024-01-15", "EUR", null);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getDate()).isEqualTo(LocalDate.of(2024, 1, 15));
        }

        @Test
        @DisplayName("Should return historical rates with symbols filter")
        void shouldReturnHistoricalRates_WithSymbolsFilter() {
            ForexRatesResponse filteredResponse = ForexRatesResponse.builder()
                    .base("EUR")
                    .date(LocalDate.of(2024, 1, 15))
                    .rates(Map.of("USD", 1.0945))
                    .build();

            when(forexService.getHistoricalRates("2024-01-15", "EUR", "USD"))
                    .thenReturn(filteredResponse);

            ResponseEntity<ForexRatesResponse> response = 
                    forexController.getHistoricalRates("2024-01-15", "EUR", "USD");

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody().getRates()).containsOnlyKeys("USD");
        }

        @Test
        @DisplayName("Should throw exception for invalid date format")
        void shouldThrowException_ForInvalidDateFormat() {
            when(forexService.getHistoricalRates("invalid-date", "EUR", null))
                    .thenThrow(new ForexApiException("Formato de fecha inválido", 400));

            assertThatThrownBy(() -> 
                    forexController.getHistoricalRates("invalid-date", "EUR", null))
                    .isInstanceOf(ForexApiException.class);
        }
    }

    @Nested
    @DisplayName("POST /forex/convert Tests")
    class ConvertCurrencyTest {

        @Test
        @DisplayName("Should convert currency successfully")
        void shouldConvertCurrency_Successfully() {
            CurrencyConversionRequest request = CurrencyConversionRequest.builder()
                    .from("EUR")
                    .to("USD")
                    .amount(BigDecimal.valueOf(100))
                    .build();

            when(forexService.convertCurrency(any(CurrencyConversionRequest.class)))
                    .thenReturn(sampleConversionResponse);

            ResponseEntity<CurrencyConversionResponse> response = 
                    forexController.convertCurrency(request);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getFrom()).isEqualTo("EUR");
            assertThat(response.getBody().getTo()).isEqualTo("USD");
            assertThat(response.getBody().getResult()).isEqualTo(BigDecimal.valueOf(115.47));
        }

        @Test
        @DisplayName("Should throw exception for invalid conversion request")
        void shouldThrowException_ForInvalidRequest() {
            CurrencyConversionRequest request = CurrencyConversionRequest.builder()
                    .from("EUR")
                    .to("EUR")
                    .amount(BigDecimal.valueOf(100))
                    .build();

            when(forexService.convertCurrency(any(CurrencyConversionRequest.class)))
                    .thenThrow(new ForexApiException("Las divisas deben ser diferentes", 400));

            assertThatThrownBy(() -> forexController.convertCurrency(request))
                    .isInstanceOf(ForexApiException.class)
                    .hasMessageContaining("diferentes");
        }

        @Test
        @DisplayName("Should throw exception for negative amount")
        void shouldThrowException_ForNegativeAmount() {
            CurrencyConversionRequest request = CurrencyConversionRequest.builder()
                    .from("EUR")
                    .to("USD")
                    .amount(BigDecimal.valueOf(-100))
                    .build();

            when(forexService.convertCurrency(any(CurrencyConversionRequest.class)))
                    .thenThrow(new ForexApiException("El monto debe ser mayor a 0", 400));

            assertThatThrownBy(() -> forexController.convertCurrency(request))
                    .isInstanceOf(ForexApiException.class);
        }
    }
}
