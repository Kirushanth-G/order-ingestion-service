package com.example.order_ingestion.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class MonthlySummaryDto {
    private BigDecimal totalGross;
    private BigDecimal totalDiscount;
    private BigDecimal totalNet;
    private Long orderCount;
}
