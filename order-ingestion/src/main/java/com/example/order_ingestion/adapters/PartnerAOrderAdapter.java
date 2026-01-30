package com.example.order_ingestion.adapters;

import com.example.order_ingestion.dtos.PartnerAOrder;
import com.example.order_ingestion.dtos.UnifiedOrderDTO;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Component
public class PartnerAOrderAdapter implements PartnerOrderAdapter<PartnerAOrder> {

    private static final String PARTNER_ID = "A";

    @Override
    public UnifiedOrderDTO toUnifiedOrder(PartnerAOrder partnerOrder) {
        LocalDateTime eventTime = convertEpochToDateTime(partnerOrder.getTransactionTimeMs());
        BigDecimal grossAmount = partnerOrder.getAmount();
        BigDecimal discount = BigDecimal.ZERO;
        BigDecimal netAmount = grossAmount; // No discount for Partner A

        return UnifiedOrderDTO.builder()
                .productCode(partnerOrder.getSkuId())
                .eventTime(eventTime)
                .grossAmount(grossAmount)
                .discount(discount)
                .netAmount(netAmount)
                .partnerId(PARTNER_ID)
                .build();
    }

    @Override
    public String getPartnerId() {
        return PARTNER_ID;
    }

    private LocalDateTime convertEpochToDateTime(Long epochMs) {
        if (epochMs == null) {
            return null;
        }
        return Instant.ofEpochMilli(epochMs)
                .atZone(ZoneId.of("UTC"))
                .toLocalDateTime();
    }
}

