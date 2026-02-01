# 🧪 API Testing Guide (Curl Commands)

This document provides a complete list of Curl commands to manually test the Order Ingestion Service.

**Base URL:** `http://localhost:8080`
**Auth Header:** `X-API-KEY` (Required for all endpoints)

---

## 1. 📥 Data Ingestion (POST)

### ✅ Ingest Partner A Order (Success)

* **Key:** `partner-a-secret-key-12345`
* **Scenario:** Standard order ingestion.

```bash
curl -v -X POST http://localhost:8080/api/orders/partnerA \
  -H "Content-Type: application/json" \
  -H "X-API-KEY: partner-a-secret-key-12345" \
  -d '{
    "skuId": "SKU-A-1001",
    "transactionTimeMs": 1738400000000,
    "amount": 150.50
  }'
```

### ✅ Ingest Partner B Order (Success with Discount)

* **Key:** `partner-b-secret-key-67890`
* **Scenario:** Order with discount applied.

```bash
curl -v -X POST http://localhost:8080/api/orders/partnerB \
  -H "Content-Type: application/json" \
  -H "X-API-KEY: partner-b-secret-key-67890" \
  -d '{
    "itemCode": "ITEM-B-99",
    "purchaseTime": "2026-02-01 10:00:00",
    "total": 200.00,
    "discount": 20.00
  }'
```

---

## 2. 📤 Data Retrieval (GET)

### 📋 Get All Valid Orders (Time Range)

Fetch orders for Partner A within a specific date range.

```bash
curl -v -X GET "http://localhost:8080/api/orders?partnerId=A&from=2024-01-01T00:00:00&to=2026-12-31T23:59:59" \
  -H "X-API-KEY: partner-a-secret-key-12345"
```

### 📊 Get Monthly Summary

View aggregated totals (Gross, Net, Discount, Count) for a specific month.

```bash
curl -v -X GET "http://localhost:8080/api/orders/summary/monthly?partnerId=B&month=02-2026" \
  -H "X-API-KEY: partner-b-secret-key-67890"
```

### 🚨 Get Error Logs

View orders that failed validation (useful for debugging).

```bash
curl -v -X GET "http://localhost:8080/api/orders/errors?partnerId=A&from=2024-01-01T00:00:00&to=2026-12-31T23:59:59" \
  -H "X-API-KEY: partner-a-secret-key-12345"
```

---

## 3. 🛡️ Edge Cases & Error Handling

### 🚫 Test Validation Failure (Negative Amount)

Sends an invalid amount to verify it is caught and logged to the error table.

```bash
curl -v -X POST http://localhost:8080/api/orders/partnerA \
  -H "Content-Type: application/json" \
  -H "X-API-KEY: partner-a-secret-key-12345" \
  -d '{
    "skuId": "ERR-001",
    "transactionTimeMs": 1738400000000,
    "amount": -50.00
  }'
```

**Expected Result:** `400 Bad Request`

---

### 🔒 Test Security Failure (Missing/Wrong Key)

Attempts to access the API without the correct credentials.

```bash
curl -v -X POST http://localhost:8080/api/orders/partnerA \
  -H "Content-Type: application/json" \
  -d '{"skuId": "TEST", "transactionTimeMs": 123, "amount": 10}'
```

**Expected Result:** `403 Forbidden`

---

### ♻️ Test Idempotency (Duplicate Prevention)

Run this command twice.

```bash
curl -v -X POST http://localhost:8080/api/orders/partnerA \
  -H "Content-Type: application/json" \
  -H "X-API-KEY: partner-a-secret-key-12345" \
  -d '{
    "skuId": "DUPLICATE-TEST",
    "transactionTimeMs": 1738400000000,
    "amount": 100.00
  }'
```

* **Run 1:** Returns `202 Accepted` (Order Saved)
* **Run 2:** Returns `202 Accepted` (Order Ignored internally - check server logs for "Duplicate Order Ignored")