package com.forex.api.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CurrencyConversionRequest {
    
    @NotBlank(message = "El campo 'from' es requerido")
    @Pattern(regexp = "^[A-Z]{3}$", message = "El código de divisa debe tener 3 letras mayúsculas")
    private String from;
    
    @NotBlank(message = "El campo 'to' es requerido")
    @Pattern(regexp = "^[A-Z]{3}$", message = "El código de divisa debe tener 3 letras mayúsculas")
    private String to;
    
    @NotNull(message = "El campo 'amount' es requerido")
    @DecimalMin(value = "0.01", message = "El monto debe ser mayor a 0")
    private BigDecimal amount;
}
