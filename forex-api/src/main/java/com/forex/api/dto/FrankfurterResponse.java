package com.forex.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FrankfurterResponse {
    
    @JsonProperty("base")
    private String base;
    
    @JsonProperty("date")
    private String date;
    
    @JsonProperty("rates")
    private Map<String, Double> rates;
}
