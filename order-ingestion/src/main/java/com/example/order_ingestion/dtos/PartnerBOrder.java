package com.example.order_ingestion.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class PartnerBOrder {
    @NotBlank(message = "itemCode is required")
    private String itemCode;

    @NotBlank(message = "purchaseTime is required")
    private String purchaseTime;

    @NotNull(message = "total is required")
    @Positive(message = "total must be greater than 0")
    private BigDecimal total;

    @PositiveOrZero(message = "discount cannot be negative")
    private BigDecimal discount;
}
