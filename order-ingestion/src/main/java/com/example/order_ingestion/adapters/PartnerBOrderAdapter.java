package com.example.order_ingestion.adapters;

import com.example.order_ingestion.dtos.PartnerBOrder;
import com.example.order_ingestion.dtos.UnifiedOrderDTO;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class PartnerBOrderAdapter implements PartnerOrderAdapter<PartnerBOrder> {

    private static final String PARTNER_ID = "B";
    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public UnifiedOrderDTO toUnifiedOrder(PartnerBOrder partnerOrder) {
        LocalDateTime eventTime = convertStringToDateTime(partnerOrder.getPurchaseTime());
        BigDecimal grossAmount = partnerOrder.getTotal();
        BigDecimal discount = partnerOrder.getDiscount() != null
                ? partnerOrder.getDiscount()
                : BigDecimal.ZERO;
        BigDecimal netAmount = grossAmount.subtract(discount);

        return UnifiedOrderDTO.builder()
                .productCode(partnerOrder.getItemCode())
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

    private LocalDateTime convertStringToDateTime(String dateStr) {
        if (dateStr == null || dateStr.isBlank()) {
            return null;
        }
        return LocalDateTime.parse(dateStr, DATE_FORMATTER);
    }
}

