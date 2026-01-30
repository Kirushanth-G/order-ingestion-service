package com.example.order_ingestion.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "order_events")
public class OrderEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "product_code", nullable = false)
    private String productCode;

    @Column(name = "event_time", nullable = false)
    private LocalDateTime eventTime;

    @Column(name = "gross_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal grossAmount;

    @Column(name = "discount", nullable = false, precision = 15, scale = 2)
    private BigDecimal discount = BigDecimal.ZERO;

    @Column(name = "net_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal netAmount;

    @Column(name = "partner_id", nullable = false, length = 10)
    private String partnerId;

    @Column(name = "sequence_number", nullable = false)
    private Long sequenceNumber;

    @Column(name = "received_time", nullable = false)
    private LocalDateTime receivedTime;

    // We set a default of 0L since we aren't using real Kafka yet
    @Column(name = "stream_offset")
    private Long streamOffset = 0L;

    @Column(name = "processed_time", nullable = false)
    private LocalDateTime processedTime;
}
