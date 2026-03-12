package com.forex.api.dto.request;

import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LatestRatesRequest {
    
    @Pattern(regexp = "^[A-Z]{3}$", message = "El código de divisa debe tener 3 letras mayúsculas")
    private String base;
    
    @Pattern(regexp = "^[A-Z]{3}(,[A-Z]{3})*$", message = "Los símbolos deben ser códigos de 3 letras separados por coma")
    private String symbols;
}
