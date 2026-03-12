package com.forex.api.service;

import com.forex.api.dto.FrankfurterResponse;
import com.forex.api.dto.request.CurrencyConversionRequest;
import com.forex.api.dto.response.CurrencyConversionResponse;
import com.forex.api.dto.response.ForexRatesResponse;
import com.forex.api.entity.ForexRate;
import com.forex.api.exception.ForexApiException;
import com.forex.api.mapper.ForexMapper;
import com.forex.api.repository.FrankfurterRepository;
import com.forex.api.validation.ForexValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ForexService Tests")
class ForexServiceTest {

    @Mock
    private FrankfurterRepository repository;

    @Mock
    private ForexMapper mapper;

    @Mock
    private ForexValidator validator;

    @InjectMocks
    private ForexService forexService;

    private FrankfurterResponse sampleResponse;
    private ForexRate sampleForexRate;
    private ForexRatesResponse sampleRatesResponse;

    @BeforeEach
    void setUp() {
        sampleResponse = FrankfurterResponse.builder()
                .base("EUR")
                .date("2026-03-12")
                .rates(Map.of("USD", 1.1547, "GBP", 0.86243))
                .build();

        sampleForexRate = ForexRate.builder()
                .base("EUR")
                .date(LocalDate.of(2026, 3, 12))
                .rates(Map.of("USD", 1.1547, "GBP", 0.86243))
                .build();

        sampleRatesResponse = ForexRatesResponse.builder()
                .base("EUR")
                .date(LocalDate.of(2026, 3, 12))
                .rates(Map.of("USD", 1.1547, "GBP", 0.86243))
                .build();
    }

    @Nested
    @DisplayName("getLatestRates Tests")
    class GetLatestRatesTest {

        @Test
        @DisplayName("Should return latest rates when valid base currency is provided")
        void shouldReturnLatestRates_WhenValidBaseCurrency() {
            when(repository.getLatestRates("EUR", null)).thenReturn(sampleResponse);
            when(mapper.toForexRate(any(FrankfurterResponse.class))).thenReturn(sampleForexRate);
            when(mapper.toForexRatesResponse(any(ForexRate.class))).thenReturn(sampleRatesResponse);

            ForexRatesResponse result = forexService.getLatestRates("EUR", null);

            assertThat(result).isNotNull();
            assertThat(result.getBase()).isEqualTo("EUR");
            assertThat(result.getRates()).containsKey("USD");
            assertThat(result.getRates()).containsKey("GBP");
        }

        @Test
        @DisplayName("Should throw exception when base currency is null")
        void shouldThrowException_WhenBaseCurrencyIsNull() {
            assertThatThrownBy(() -> forexService.getLatestRates(null, null))
                    .isInstanceOf(ForexApiException.class)
                    .hasMessageContaining("Error de validación");
        }

        @Test
        @DisplayName("Should throw exception when base currency is empty")
        void shouldThrowException_WhenBaseCurrencyIsEmpty() {
            assertThatThrownBy(() -> forexService.getLatestRates("", null))
                    .isInstanceOf(ForexApiException.class)
                    .hasMessageContaining("Error de validación");
        }

        @Test
        @DisplayName("Should return rates with symbols filter using stream")
        void shouldFilterRatesWithSymbols() {
            when(repository.getLatestRates("EUR", "USD")).thenReturn(sampleResponse);
            when(mapper.toForexRate(any(FrankfurterResponse.class))).thenReturn(sampleForexRate);
            when(mapper.toForexRatesResponse(any(ForexRate.class))).thenReturn(sampleRatesResponse);

            ForexRatesResponse result = forexService.getLatestRates("EUR", "USD");

            assertThat(result.getRates()).containsKey("USD");
        }
    }

    @Nested
    @DisplayName("getHistoricalRates Tests")
    class GetHistoricalRatesTest {

        @Test
        @DisplayName("Should return historical rates when valid date is provided")
        void shouldReturnHistoricalRates_WhenValidDate() {
            when(repository.getHistoricalRates("2024-01-15", "EUR", null)).thenReturn(sampleResponse);
            when(mapper.toForexRate(any(FrankfurterResponse.class))).thenReturn(sampleForexRate);
            when(mapper.toForexRatesResponse(any(ForexRate.class))).thenReturn(sampleRatesResponse);

            ForexRatesResponse result = forexService.getHistoricalRates("2024-01-15", "EUR", null);

            assertThat(result).isNotNull();
            assertThat(result.getBase()).isEqualTo("EUR");
        }

        @Test
        @DisplayName("Should throw exception when date is null")
        void shouldThrowException_WhenDateIsNull() {
            assertThatThrownBy(() -> forexService.getHistoricalRates(null, "EUR", null))
                    .isInstanceOf(ForexApiException.class);
        }

        @Test
        @DisplayName("Should throw exception when date is invalid")
        void shouldThrowException_WhenDateIsInvalid() {
            assertThatThrownBy(() -> forexService.getHistoricalRates("invalid-date", "EUR", null))
                    .isInstanceOf(ForexApiException.class);
        }
    }

    @Nested
    @DisplayName("convertCurrency Tests")
    class ConvertCurrencyTest {

        @Test
        @DisplayName("Should convert currency successfully")
        void shouldConvertCurrency_Successfully() {
            CurrencyConversionRequest request = CurrencyConversionRequest.builder()
                    .from("EUR")
                    .to("USD")
                    .amount(BigDecimal.valueOf(100))
                    .build();

            when(repository.getLatestRates("EUR", "USD")).thenReturn(sampleResponse);

            CurrencyConversionResponse result = forexService.convertCurrency(request);

            assertThat(result).isNotNull();
            assertThat(result.getFrom()).isEqualTo("EUR");
            assertThat(result.getTo()).isEqualTo("USD");
            assertThat(result.getAmount()).isEqualTo(BigDecimal.valueOf(100));
            assertThat(result.getRate()).isEqualTo(BigDecimal.valueOf(1.1547));
            assertThat(result.getResult()).isEqualTo(BigDecimal.valueOf(115.47));
        }

        @Test
        @DisplayName("Should throw exception when conversion request is null")
        void shouldThrowException_WhenRequestIsNull() {
            assertThatThrownBy(() -> forexService.convertCurrency(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("Should throw exception when source and target currencies are the same")
        void shouldThrowException_WhenCurrenciesAreSame() {
            CurrencyConversionRequest request = CurrencyConversionRequest.builder()
                    .from("EUR")
                    .to("EUR")
                    .amount(BigDecimal.valueOf(100))
                    .build();

            assertThatThrownBy(() -> forexService.convertCurrency(request))
                    .isInstanceOf(ForexApiException.class);
        }

        @Test
        @DisplayName("Should throw exception when amount is zero")
        void shouldThrowException_WhenAmountIsZero() {
            CurrencyConversionRequest request = CurrencyConversionRequest.builder()
                    .from("EUR")
                    .to("USD")
                    .amount(BigDecimal.ZERO)
                    .build();

            assertThatThrownBy(() -> forexService.convertCurrency(request))
                    .isInstanceOf(ForexApiException.class);
        }

        @Test
        @DisplayName("Should throw exception when amount is negative")
        void shouldThrowException_WhenAmountIsNegative() {
            CurrencyConversionRequest request = CurrencyConversionRequest.builder()
                    .from("EUR")
                    .to("USD")
                    .amount(BigDecimal.valueOf(-100))
                    .build();

            assertThatThrownBy(() -> forexService.convertCurrency(request))
                    .isInstanceOf(ForexApiException.class);
        }
    }

    @Nested
    @DisplayName("getRatesWithFilter Tests")
    class GetRatesWithFilterTest {

        @Test
        @DisplayName("Should filter rates using predicate")
        void shouldFilterRates_UsingPredicate() {
            when(repository.getLatestRates("EUR", null)).thenReturn(sampleResponse);

            Map<String, Double> result = forexService.getRatesWithFilter("EUR", null, 
                currency -> currency.startsWith("U"));

            assertThat(result).containsKey("USD");
            assertThat(result).doesNotContainKey("GBP");
        }

        @Test
        @DisplayName("Should return empty map when no rates match filter")
        void shouldReturnEmptyMap_WhenNoRatesMatch() {
            when(repository.getLatestRates("EUR", null)).thenReturn(sampleResponse);

            Map<String, Double> result = forexService.getRatesWithFilter("EUR", null, 
                currency -> currency.startsWith("X"));

            assertThat(result).isEmpty();
        }
    }
}
