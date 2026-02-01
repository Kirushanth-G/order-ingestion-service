package com.example.order_ingestion.controllers;

import com.example.order_ingestion.dtos.MonthlySummaryDto;
import com.example.order_ingestion.dtos.PartnerAOrder;
import com.example.order_ingestion.dtos.PartnerBOrder;
import com.example.order_ingestion.entities.ErrorOrder;
import com.example.order_ingestion.entities.OrderEvent;
import com.example.order_ingestion.repositories.ErrorOrderRepository;
import com.example.order_ingestion.repositories.OrderEventRepository;
import com.example.order_ingestion.services.OrderIngestionService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@AllArgsConstructor
@RequestMapping("/api/orders")
public class OrderIngestionController {
    private final OrderIngestionService orderIngestionService;
    private final OrderEventRepository orderEventRepository;
    private final ErrorOrderRepository errorOrderRepository;

    @PostMapping("/partnerA")
    public ResponseEntity<String> ingestPartnerA(@Valid @RequestBody PartnerAOrder order, BindingResult result) {
        if (result.hasErrors()) {
            return handleValidationErrors("A", order.toString(), result);
        }

        orderIngestionService.ingestOrder("A", order);

        return ResponseEntity
                .accepted() // HTTP 202 Accepted
                .body("Order accepted for processing");
    }

    @PostMapping("/partnerB")
    public ResponseEntity<String> ingestPartnerB(@Valid @RequestBody PartnerBOrder order, BindingResult result) {
        if (result.hasErrors()) {
            return handleValidationErrors("B", order.toString(), result);
        }
        orderIngestionService.ingestOrder("B", order);

        return ResponseEntity
                .accepted() // HTTP 202 Accepted
                .body("Order accepted for processing");
    }

    private ResponseEntity<String> handleValidationErrors(String partnerId, String payload, BindingResult result) {
        var errors = result.getFieldErrors().stream()
                .map(err -> err.getField() + ": " + err.getDefaultMessage())
                .toList();
        orderIngestionService.recordError(partnerId, payload, errors);
        return ResponseEntity
                .badRequest()
                .body("Validation failed: " + String.join("; ", errors));
    }

    @GetMapping
    public ResponseEntity<List<OrderEvent>> getOrders(
            @RequestParam(required = false) String partnerId,
            @RequestParam LocalDateTime from,
            @RequestParam LocalDateTime to) {
        List<OrderEvent> orders = orderEventRepository.findByPartnerIdAndReceivedTimeBetween(partnerId, from, to);
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/summary/monthly")
    public ResponseEntity<?> getMonthlyReport(
            @RequestParam String partnerId,
            @RequestParam String month) {
        YearMonth yearMonth;
        try {
            yearMonth = YearMonth.parse(month, DateTimeFormatter.ofPattern("MM-yyyy"));
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid date format. Required: MM-yyyy");
        }

        LocalDateTime start = yearMonth.atDay(1).atStartOfDay();
        LocalDateTime end = yearMonth.plusMonths(1).atDay(1).atStartOfDay();
        MonthlySummaryDto summary = orderEventRepository.findMonthlySummary(partnerId, start, end);

        return ResponseEntity.ok(summary);
    }

    @GetMapping("/errors")
    public ResponseEntity<List<ErrorOrder>> getValidationErrors(
            @RequestParam String partnerId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {

        List<ErrorOrder> errors = errorOrderRepository.findByPartnerIdAndReceivedTimeBetween(partnerId, from, to);
        return ResponseEntity.ok(errors);
    }
}
