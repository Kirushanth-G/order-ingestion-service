package com.example.order_ingestion.services;

import com.example.order_ingestion.adapters.PartnerAdapterFactory;
import com.example.order_ingestion.adapters.PartnerOrderAdapter;
import com.example.order_ingestion.dtos.UnifiedOrderDTO;
import com.example.order_ingestion.entities.OrderEvent;
import com.example.order_ingestion.mappers.OrderMapper;
import com.example.order_ingestion.queue.OrderQueue;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@AllArgsConstructor
@Service
public class OrderIngestionService {
    private final PartnerAdapterFactory adapterFactory;
    private final OrderMapper orderMapper;
    private final OrderQueue orderQueue;

    public void ingestOrder(String partnerId, Object partnerOrder) {
        PartnerOrderAdapter<Object> adapter = (PartnerOrderAdapter<Object>) adapterFactory.getAdapter(partnerId);
        UnifiedOrderDTO unifiedOrderDTO = adapter.toUnifiedOrder(partnerOrder);

        OrderEvent orderEvent = orderMapper.toEntity(unifiedOrderDTO);
        orderEvent.setReceivedTime(LocalDateTime.now());
        orderEvent.setProcessedTime(LocalDateTime.now());
        orderQueue.publishValidOrder(orderEvent);
    }
}
