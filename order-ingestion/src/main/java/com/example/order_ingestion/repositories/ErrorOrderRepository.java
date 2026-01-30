package com.example.order_ingestion.repositories;

import com.example.order_ingestion.entities.ErrorOrder;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ErrorOrderRepository extends JpaRepository<ErrorOrder, Long> {
}
