package com.example.order_ingestion.controllers;

import com.example.order_ingestion.services.OrderIngestionService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Collections;
import java.util.List;

@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {
    private final OrderIngestionService ingestionService;

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<?> handleMalformedJson(HttpMessageNotReadableException e, HttpServletRequest request) {
        String uri = request.getRequestURI();
        String partnerId = "UNKNOWN";
        if (uri.contains("/partnerA")) partnerId = "A";
        else if (uri.contains("/partnerB")) partnerId = "B";

        String errorMessage = "Malformed JSON request: " + e.getMessage();
        List<String> errors = Collections.singletonList("Invalid JSON format");
        ingestionService.recordError(partnerId, "Raw Payload Unavailable (Malformed JSON)", errors);
        return ResponseEntity.badRequest().body(errorMessage);
    }
}
