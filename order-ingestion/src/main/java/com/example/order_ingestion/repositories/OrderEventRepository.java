package com.example.order_ingestion.repositories;

import com.example.order_ingestion.dtos.MonthlySummaryDto;
import com.example.order_ingestion.entities.OrderEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface OrderEventRepository extends JpaRepository<OrderEvent, Long> {
    @Query("SELECT MAX(o.sequenceNumber) FROM OrderEvent o WHERE o.partnerId = :partnerId")
    Long findMaxSequenceNumber(String partnerId);

    List<OrderEvent> findByPartnerIdAndReceivedTimeBetween(String partnerId, LocalDateTime from, LocalDateTime to);

    @Query("""
        SELECT new com.example.order_ingestion.dtos.MonthlySummaryDto(
            COALESCE(SUM(o.grossAmount), 0), 
            COALESCE(SUM(o.discount), 0), 
            COALESCE(SUM(o.netAmount), 0), 
            COUNT(o)
        )
        FROM OrderEvent o 
        WHERE o.partnerId = :partnerId 
          AND o.eventTime >= :start 
          AND o.eventTime < :end
    """)
    MonthlySummaryDto findMonthlySummary(
            @Param("partnerId") String partnerId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );
}
