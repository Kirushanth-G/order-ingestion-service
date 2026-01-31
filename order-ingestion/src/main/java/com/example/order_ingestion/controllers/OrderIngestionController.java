package com.example.order_ingestion.controllers;

import com.example.order_ingestion.dtos.PartnerAOrder;
import com.example.order_ingestion.dtos.PartnerBOrder;
import com.example.order_ingestion.repositories.OrderEventRepository;
import com.example.order_ingestion.services.OrderIngestionService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@RequestMapping("/orders")
public class OrderIngestionController {
    private final OrderIngestionService orderIngestionService;
    private final OrderEventRepository orderEventRepository;

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
        if( result.hasErrors()) {
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


}
