package com.example.order_ingestion.repositories;

import com.example.order_ingestion.entities.OrderEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface OrderEventRepository extends JpaRepository<OrderEvent, Long> {
    @Query("SELECT MAX(o.sequenceNumber) FROM OrderEvent o WHERE o.partnerId = :partnerId")
    Long findMaxSequenceNumber(String partnerId);
}
