package com.example.order_ingestion.services;

import com.example.order_ingestion.entities.OrderEvent;
import com.example.order_ingestion.queue.OrderQueue;
import com.example.order_ingestion.repositories.ErrorOrderRepository;
import com.example.order_ingestion.repositories.OrderEventRepository;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class OrderProcessorService {
    private final OrderQueue orderQueue;
    private final OrderEventRepository orderEventRepository;
    private final ErrorOrderRepository errorOrderRepository;

    private volatile boolean running = true;
    private Thread validConsumerThread;
    private Thread errorConsumerThread;

    @PostConstruct
    public void startProcessing(){
        validConsumerThread = new Thread(this::processValidOrders, "Valid-Consumer");
        validConsumerThread.start();

        errorConsumerThread = new Thread(this::processErrorOrders, "Error-Consumer");
        errorConsumerThread.start();
    }
    @PreDestroy
    public void stopConsumers() {
        log.info("Stopping Order Consumers...");
        running = false;
        if (validConsumerThread != null) validConsumerThread.interrupt();
        if (errorConsumerThread != null) errorConsumerThread.interrupt();
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
            orderEventRepository.save(order);
            log.info("Saved Order: Partner={}, Seq={}", order.getPartnerId(), order.getSequenceNumber());

        } catch (DataIntegrityViolationException e) {
            log.warn("Duplicate Order Ignored: Partner={}, Seq={}",
                    order.getPartnerId(), order.getSequenceNumber());
        }
    }

    public void processErrorOrders() {
        while (running) {
            try {
                var errorOrder = orderQueue.consumeErrorOrder();
                errorOrderRepository.save(errorOrder);
                log.info("Saved Error Order: Partner={}, Time={}",
                        errorOrder.getPartnerId(), errorOrder.getReceivedTime());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.info("Error consumer interrupted, shutting down.");
                break;
            } catch (Exception e) {
                log.error("Unexpected error in error consumer", e);
            }
        }
    }
}
