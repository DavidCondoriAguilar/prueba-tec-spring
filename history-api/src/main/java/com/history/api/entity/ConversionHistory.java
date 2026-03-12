package com.history.api.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "conversion_history")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConversionHistory {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "from_currency", nullable = false)
    private String fromCurrency;
    
    @Column(name = "to_currency", nullable = false)
    private String toCurrency;
    
    @Column(nullable = false)
    private BigDecimal amount;
    
    @Column(nullable = false)
    private BigDecimal result;
    
    @Column(nullable = false)
    private BigDecimal rate;
    
    @Column(name = "conversion_date", nullable = false)
    private LocalDateTime conversionDate;
    
    @Column(name = "user_id")
    private String userId;
}
