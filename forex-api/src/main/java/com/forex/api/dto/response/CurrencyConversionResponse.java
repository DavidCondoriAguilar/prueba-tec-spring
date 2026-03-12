package com.forex.api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CurrencyConversionResponse {
    
    private String from;
    private String to;
    private BigDecimal amount;
    private BigDecimal rate;
    private BigDecimal result;
    private LocalDate date;
}
