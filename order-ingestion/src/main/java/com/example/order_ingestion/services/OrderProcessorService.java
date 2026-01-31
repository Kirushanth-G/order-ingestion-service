package com.example.order_ingestion.services;

import com.example.order_ingestion.entities.OrderEvent;
import com.example.order_ingestion.queue.OrderQueue;
import com.example.order_ingestion.repositories.OrderEventRepository;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

@Slf4j
@AllArgsConstructor
@Service
public class OrderProcessorService {
    private final OrderQueue orderQueue;
    private final OrderEventRepository orderEventRepository;

    private volatile boolean running = true;
    private Thread validConsumerThread;

    @PostConstruct
    public void startProcessing(){
        validConsumerThread = new Thread(this::processValidOrders, "Valid-Consumer");
        validConsumerThread.start();
    }
    @PreDestroy
    public void stopConsumers() {
        log.info("Stopping Order Consumers...");
        running = false;
        if (validConsumerThread != null) validConsumerThread.interrupt();
    }

    private void processValidOrders() {
        while (running) {
            try {
                OrderEvent order = orderQueue.consumeValidOrder();
                processSingleOrder(order);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.info("Valid consumer interrupted, shutting down.");
                break;
            } catch (Exception e) {
                log.error("Unexpected error in consumer", e);
            }
        }
    }

    @Transactional
    public void processSingleOrder(OrderEvent order) {
        try {
            Long currentMax = orderEventRepository.findMaxSequenceNumber(order.getPartnerId());
            long nextSeq = (currentMax == null ? 0 : currentMax) + 1;
            order.setSequenceNumber(nextSeq);

            orderEventRepository.save(order);
            log.info("✅ Saved Order: Partner={}, Seq={}", order.getPartnerId(), nextSeq);

        } catch (DataIntegrityViolationException e) {
            log.warn("⚠️ Duplicate Order Ignored: Partner={}, Seq={}",
                    order.getPartnerId(), order.getSequenceNumber());
        }
    }
}
