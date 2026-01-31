package com.example.order_ingestion.queue;

import com.example.order_ingestion.entities.ErrorOrder;
import com.example.order_ingestion.entities.OrderEvent;
import org.springframework.stereotype.Component;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@Component
public class OrderQueue {
    private final BlockingQueue<OrderEvent> validQueue = new LinkedBlockingQueue<>();
    private final BlockingQueue<ErrorOrder> errorQueue = new LinkedBlockingQueue<>();

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

    public void publishErrorOrder(ErrorOrder errorOrder){
        try {
            errorQueue.put(errorOrder);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Failed to publishErrorOrder order event", e);
        }
    }

    public ErrorOrder consumeErrorOrder() throws InterruptedException {
        return errorQueue.take();
    }
}
