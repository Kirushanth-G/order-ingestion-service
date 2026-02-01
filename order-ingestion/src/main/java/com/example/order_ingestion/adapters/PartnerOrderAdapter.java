package com.example.order_ingestion.adapters;

import com.example.order_ingestion.dtos.UnifiedOrderDto;

public interface PartnerOrderAdapter<T> {
    UnifiedOrderDto toUnifiedOrder(T partnerOrder);

    String getPartnerId();
}

