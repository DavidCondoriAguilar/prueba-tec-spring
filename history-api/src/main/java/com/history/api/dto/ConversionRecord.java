package com.history.api.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ConversionRecord(
    Long id,
    String fromCurrency,
    String toCurrency,
    BigDecimal amount,
    BigDecimal result,
    BigDecimal rate,
    LocalDateTime conversionDate,
    String userId
) {}
