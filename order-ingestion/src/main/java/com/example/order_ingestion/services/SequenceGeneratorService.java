package com.example.order_ingestion.services;

import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class SequenceGeneratorService {
    private final ConcurrentHashMap<String, AtomicLong> sequences = new ConcurrentHashMap<>();
    public long getNextSequence(String partnerId) {
        AtomicLong sequence = sequences.computeIfAbsent(partnerId, k -> new AtomicLong(0));
        return sequence.incrementAndGet();
    }
}
