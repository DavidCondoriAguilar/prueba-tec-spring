package com.forex.api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ForexRatesResponse {
    
    private String base;
    private LocalDate date;
    private Map<String, Double> rates;
}
