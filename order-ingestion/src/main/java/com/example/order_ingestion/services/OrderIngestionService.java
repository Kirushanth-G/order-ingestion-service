package com.example.order_ingestion.services;

import com.example.order_ingestion.adapters.PartnerAdapterFactory;
import com.example.order_ingestion.adapters.PartnerOrderAdapter;
import com.example.order_ingestion.dtos.UnifiedOrderDto;
import com.example.order_ingestion.entities.ErrorOrder;
import com.example.order_ingestion.entities.OrderEvent;
import com.example.order_ingestion.mappers.OrderMapper;
import com.example.order_ingestion.queue.OrderQueue;
import com.example.order_ingestion.repositories.OrderEventRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@AllArgsConstructor
@Service
public class OrderIngestionService {
    private final PartnerAdapterFactory adapterFactory;
    private final OrderMapper orderMapper;
    private final OrderQueue orderQueue;
    private final SequenceGeneratorService sequenceGeneratorService;
    private final OrderEventRepository orderEventRepository;

    public void ingestOrder(String partnerId, Object partnerOrder) {
        PartnerOrderAdapter<Object> adapter = (PartnerOrderAdapter<Object>) adapterFactory.getAdapter(partnerId);
        UnifiedOrderDto unifiedOrderDTO = adapter.toUnifiedOrder(partnerOrder);

        if (orderEventRepository.existsByPartnerIdAndProductCodeAndEventTime(
                unifiedOrderDTO.getPartnerId(),
                unifiedOrderDTO.getProductCode(),
                unifiedOrderDTO.getEventTime())) {

            log.info("Duplicate Order Ignored (Idempotent): Partner={}, Product={}, Time={}",
                    unifiedOrderDTO.getPartnerId(),
                    unifiedOrderDTO.getProductCode(),
                    unifiedOrderDTO.getEventTime());
            return; // Stop processing safely
        }

        OrderEvent orderEvent = orderMapper.toEntity(unifiedOrderDTO);
        orderEvent.setReceivedTime(LocalDateTime.now());
        orderEvent.setProcessedTime(LocalDateTime.now());
        long seq = sequenceGeneratorService.getNextSequence(partnerId);
        orderEvent.setSequenceNumber(seq);

        orderQueue.publishValidOrder(orderEvent);
    }

    public void recordError(String partnerId, String payload, List<String> errors){
        String errorReasons = String.join("; ", errors);
        ErrorOrder errorOrder = new ErrorOrder();
        errorOrder.setPartnerId(partnerId);
        errorOrder.setReceivedTime(LocalDateTime.now());
        errorOrder.setRawPayload(payload);
        errorOrder.setErrorReasons(errorReasons);
        orderQueue.publishErrorOrder(errorOrder);
    }
}
