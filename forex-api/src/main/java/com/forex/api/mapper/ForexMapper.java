package com.forex.api.mapper;

import com.forex.api.dto.FrankfurterResponse;
import com.forex.api.dto.response.ForexRatesResponse;
import com.forex.api.entity.ForexRate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Component
@Slf4j
public class ForexMapper {
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    
    public ForexRate toForexRate(FrankfurterResponse response) {
        log.debug("Converting FrankfurterResponse to ForexRate: {}", response);
        
        if (response == null) {
            log.warn("FrankfurterResponse is null, returning null");
            return null;
        }
        
        LocalDate date = LocalDate.parse(response.getDate(), DATE_FORMATTER);
        ForexRate forexRate = ForexRate.builder()
                .base(response.getBase())
                .date(date)
                .rates(response.getRates())
                .build();
        
        log.debug("Successfully converted to ForexRate: {}", forexRate);
        return forexRate;
    }
    
    public FrankfurterResponse fromForexRate(ForexRate forexRate) {
        log.debug("Converting ForexRate to FrankfurterResponse: {}", forexRate);
        
        if (forexRate == null) {
            log.warn("ForexRate is null, returning null");
            return null;
        }
        
        FrankfurterResponse response = FrankfurterResponse.builder()
                .base(forexRate.getBase())
                .date(forexRate.getDate().format(DATE_FORMATTER))
                .rates(forexRate.getRates())
                .build();
        
        log.debug("Successfully converted to FrankfurterResponse: {}", response);
        return response;
    }
    
    public ForexRatesResponse toForexRatesResponse(ForexRate forexRate) {
        log.debug("Converting ForexRate to ForexRatesResponse: {}", forexRate);
        
        if (forexRate == null) {
            log.warn("ForexRate is null, returning null");
            return null;
        }
        
        ForexRatesResponse response = ForexRatesResponse.builder()
                .base(forexRate.getBase())
                .date(forexRate.getDate())
                .rates(forexRate.getRates())
                .build();
        
        log.debug("Successfully converted to ForexRatesResponse: {}", response);
        return response;
    }
}
