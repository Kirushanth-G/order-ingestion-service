package com.example.order_ingestion.adapters;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class PartnerAdapterFactory {

    private final Map<String, PartnerOrderAdapter<?>> adapters;

    public PartnerAdapterFactory(List<PartnerOrderAdapter<?>> adapterList) {
        this.adapters = new HashMap<>();
        adapterList.forEach(adapter ->
            adapters.put(adapter.getPartnerId(), adapter)
        );
    }

    public PartnerOrderAdapter<?> getAdapter(String partnerId) {
        PartnerOrderAdapter<?> adapter = adapters.get(partnerId);
        if (adapter == null) {
            throw new IllegalArgumentException("No adapter found for partner: " + partnerId);
        }
        return adapter;
    }

    public boolean hasAdapter(String partnerId) {
        return adapters.containsKey(partnerId);
    }
}

