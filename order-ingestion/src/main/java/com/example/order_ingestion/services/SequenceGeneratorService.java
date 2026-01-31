package com.example.order_ingestion.services;

import com.example.order_ingestion.repositories.OrderEventRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@AllArgsConstructor
@Service
public class SequenceGeneratorService {
    private final ConcurrentHashMap<String, AtomicLong> sequences = new ConcurrentHashMap<>();
    private final OrderEventRepository repository;

    public long getNextSequence(String partnerId) {
        AtomicLong sequence = sequences.computeIfAbsent(partnerId, k -> {
            Long maxSeq = repository.findMaxSequenceNumber(k);
            long startFrom = maxSeq == null ? 0 : maxSeq;
            log.info("🔹 Initialized Sequence for Partner {}: Starting from {}", k, startFrom);
            return new AtomicLong(startFrom);
        });
        return sequence.incrementAndGet();
    }
}
