package com.example.order_ingestion.repositories;

import com.example.order_ingestion.entities.OrderEvent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderEventRepository extends JpaRepository<OrderEvent, Long> {

    Long findMaxSequenceNumber(String partnerId);
}
