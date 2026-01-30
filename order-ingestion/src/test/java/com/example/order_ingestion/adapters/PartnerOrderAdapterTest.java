package com.example.order_ingestion.adapters;

import com.example.order_ingestion.dtos.PartnerAOrder;
import com.example.order_ingestion.dtos.PartnerBOrder;
import com.example.order_ingestion.dtos.UnifiedOrderDTO;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for Partner Order Adapters.
 * Demonstrates that adapters correctly convert partner-specific formats to unified format.
 */
class PartnerOrderAdapterTest {

    private final PartnerAOrderAdapter partnerAAdapter = new PartnerAOrderAdapter();
    private final PartnerBOrderAdapter partnerBAdapter = new PartnerBOrderAdapter();

    @Test
    void testPartnerAAdapter_convertsSuccessfully() {
        // Given: Partner A order with epoch timestamp
        PartnerAOrder partnerAOrder = new PartnerAOrder();
        partnerAOrder.setSkuId("SKU-1001");
        partnerAOrder.setTransactionTimeMs(1733059200123L); // 2024-12-01 12:00:00.123 UTC
        partnerAOrder.setAmount(new BigDecimal("25.50"));

        // When: Convert to unified format
        UnifiedOrderDTO unified = partnerAAdapter.toUnifiedOrder(partnerAOrder);

        // Then: All fields mapped correctly
        assertNotNull(unified);
        assertEquals("SKU-1001", unified.getProductCode());
        assertEquals(new BigDecimal("25.50"), unified.getGrossAmount());
        assertEquals(BigDecimal.ZERO, unified.getDiscount());
        assertEquals(new BigDecimal("25.50"), unified.getNetAmount()); // No discount
        assertEquals("A", unified.getPartnerId());

        // Verify timestamp was converted (exact time depends on UTC conversion)
        assertNotNull(unified.getEventTime());
        // The timestamp should be in December 2024
        assertEquals(2024, unified.getEventTime().getYear());
        assertEquals(12, unified.getEventTime().getMonthValue());
        assertEquals(1, unified.getEventTime().getDayOfMonth());
    }

    @Test
    void testPartnerBAdapter_withDiscount() {
        // Given: Partner B order with discount
        PartnerBOrder partnerBOrder = new PartnerBOrder();
        partnerBOrder.setItemCode("IT-900");
        partnerBOrder.setPurchaseTime("2026-01-28 10:12:30");
        partnerBOrder.setTotal(new BigDecimal("100.00"));
        partnerBOrder.setDiscount(new BigDecimal("10.00"));

        // When: Convert to unified format
        UnifiedOrderDTO unified = partnerBAdapter.toUnifiedOrder(partnerBOrder);

        // Then: All fields mapped correctly including discount calculation
        assertNotNull(unified);
        assertEquals("IT-900", unified.getProductCode());
        assertEquals(new BigDecimal("100.00"), unified.getGrossAmount());
        assertEquals(new BigDecimal("10.00"), unified.getDiscount());
        assertEquals(new BigDecimal("90.00"), unified.getNetAmount()); // 100 - 10
        assertEquals("B", unified.getPartnerId());

        // Verify timestamp parsing
        LocalDateTime expectedTime = LocalDateTime.of(2026, 1, 28, 10, 12, 30);
        assertEquals(expectedTime, unified.getEventTime());
    }

    @Test
    void testPartnerBAdapter_withoutDiscount() {
        // Given: Partner B order without discount
        PartnerBOrder partnerBOrder = new PartnerBOrder();
        partnerBOrder.setItemCode("IT-901");
        partnerBOrder.setPurchaseTime("2026-01-30 15:30:00");
        partnerBOrder.setTotal(new BigDecimal("50.00"));
        partnerBOrder.setDiscount(null); // No discount

        // When: Convert to unified format
        UnifiedOrderDTO unified = partnerBAdapter.toUnifiedOrder(partnerBOrder);

        // Then: Discount defaults to zero
        assertNotNull(unified);
        assertEquals(BigDecimal.ZERO, unified.getDiscount());
        assertEquals(new BigDecimal("50.00"), unified.getNetAmount()); // 50 - 0
    }

    @Test
    void testPartnerAAdapter_returnsCorrectPartnerId() {
        assertEquals("A", partnerAAdapter.getPartnerId());
    }

    @Test
    void testPartnerBAdapter_returnsCorrectPartnerId() {
        assertEquals("B", partnerBAdapter.getPartnerId());
    }
}

