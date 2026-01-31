package com.example.order_ingestion.dtos;

import jakarta.validation.constraints.*;
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

    @AssertTrue(message = "discount cannot exceed total")
    public boolean isDiscountValid() {
        if (discount == null) return true;
        return discount.compareTo(total) <= 0;
    }
}
