package com.example.order_ingestion.adapters;

import com.example.order_ingestion.dtos.UnifiedOrderDTO;

public interface PartnerOrderAdapter<T> {
    UnifiedOrderDTO toUnifiedOrder(T partnerOrder);

    String getPartnerId();
}

