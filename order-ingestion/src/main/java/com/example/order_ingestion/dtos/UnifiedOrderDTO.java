package com.example.order_ingestion.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UnifiedOrderDTO {
    private String productCode;
    private LocalDateTime eventTime;
    private BigDecimal grossAmount;
    private BigDecimal discount;
    private BigDecimal netAmount;
    private String partnerId;
}

