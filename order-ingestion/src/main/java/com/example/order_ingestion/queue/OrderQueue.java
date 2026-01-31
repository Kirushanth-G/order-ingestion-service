package com.example.order_ingestion.queue;

import com.example.order_ingestion.entities.OrderEvent;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class OrderQueue {
    private final BlockingQueue<OrderEvent> validQueue = new LinkedBlockingQueue<>();

    public void publishValidOrder(OrderEvent orderEvent){
        try {
            validQueue.put(orderEvent);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Failed to publishValidOrder order event", e);
        }
    }

    public OrderEvent consumeValidOrder() throws InterruptedException {
        return validQueue.take();
    }
}
