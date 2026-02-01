package com.example.order_ingestion.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Validates API keys for partners.
 *
 * In production/AWS:
 * - API keys should be stored in AWS Secrets Manager or Parameter Store
 * - Keys should be rotated regularly
 * - Consider using AWS API Gateway with API key management
 * - Add rate limiting per API key
 * - Log all authentication attempts for security auditing
 */
@Slf4j
@Component
public class PartnerApiKeyValidator {

    // Hardcoded API keys for simplified implementation
    // In production: Load from AWS Secrets Manager, encrypted config, or database
    private static final Map<String, String> API_KEY_TO_PARTNER = Map.of(
            "partner-a-secret-key-12345", "A",
            "partner-b-secret-key-67890", "B"
    );

    /**
     * Validates the API key and returns the partner ID if valid.
     *
     * @param apiKey The API key from the request header
     * @return Partner ID if valid, null otherwise
     */
    public String validateAndGetPartnerId(String apiKey) {
        String partnerId = API_KEY_TO_PARTNER.get(apiKey);

        if (partnerId != null) {
            log.debug("Valid API key for partner: {}", partnerId);
            return partnerId;
        }

        log.warn("Invalid API key attempt: {}", apiKey.substring(0, Math.min(10, apiKey.length())) + "***");
        return null;
    }

    /**
     * Check if a partner ID has a valid API key configured.
     *
     * @param partnerId The partner ID to check
     * @return true if the partner has an API key configured
     */
    public boolean isPartnerConfigured(String partnerId) {
        return API_KEY_TO_PARTNER.containsValue(partnerId);
    }
}

