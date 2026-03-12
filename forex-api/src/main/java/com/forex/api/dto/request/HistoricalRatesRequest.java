package com.forex.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HistoricalRatesRequest {
    
    @NotBlank(message = "El parámetro 'date' es requerido")
    @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$", message = "La fecha debe tener formato YYYY-MM-DD")
    private String date;
    
    @Pattern(regexp = "^[A-Z]{3}$", message = "El código de divisa debe tener 3 letras mayúsculas")
    private String base;
    
    @Pattern(regexp = "^[A-Z]{3}(,[A-Z]{3})*$", message = "Los símbolos deben ser códigos de 3 letras separados por coma")
    private String symbols;
}
