package com.example.order_ingestion.repositories;

import com.example.order_ingestion.entities.ErrorOrder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface ErrorOrderRepository extends JpaRepository<ErrorOrder, Long> {
    List<ErrorOrder> findByPartnerIdAndReceivedTimeBetween(String partnerId, LocalDateTime from, LocalDateTime to);
}
