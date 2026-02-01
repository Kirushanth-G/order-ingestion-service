package com.example.order_ingestion.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Slf4j
@Component
@RequiredArgsConstructor
public class ApiKeyAuthenticationFilter extends OncePerRequestFilter {

    private final PartnerApiKeyValidator partnerApiKeyValidator;

    private static final String API_KEY_HEADER = "X-API-Key";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // Skip authentication for non-protected endpoints
        String path = request.getRequestURI();
        if (!path.startsWith("/api/orders")) {
            filterChain.doFilter(request, response);
            return;
        }

        String apiKey = request.getHeader(API_KEY_HEADER);

        if (apiKey == null || apiKey.trim().isEmpty()) {
            log.warn("Missing API key for request to: {}", path);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"Missing API key. Please provide X-API-Key header.\"}");
            return;
        }

        String partnerId = partnerApiKeyValidator.validateAndGetPartnerId(apiKey);

        if (partnerId == null) {
            log.warn("Invalid API key provided for request to: {}", path);
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"Invalid API key.\"}");
            return;
        }

        // Set authentication in SecurityContext
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        partnerId,
                        null,
                        Collections.singletonList(new SimpleGrantedAuthority("ROLE_PARTNER"))
                );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        log.debug("Authenticated partner: {} for request: {}", partnerId, path);

        filterChain.doFilter(request, response);
    }
}

