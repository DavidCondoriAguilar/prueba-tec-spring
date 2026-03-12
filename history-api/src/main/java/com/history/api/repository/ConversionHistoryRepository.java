package com.history.api.repository;

import com.history.api.entity.ConversionHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ConversionHistoryRepository extends JpaRepository<ConversionHistory, Long> {
    
    List<ConversionHistory> findByUserIdOrderByConversionDateDesc(String userId);
    
    List<ConversionHistory> findByConversionDateBetween(LocalDateTime start, LocalDateTime end);
}
