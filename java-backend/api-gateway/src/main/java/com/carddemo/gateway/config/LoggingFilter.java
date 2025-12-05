package com.carddemo.gateway.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Instant;

/**
 * Global logging filter for request/response tracking.
 * 
 * Logs all requests passing through the gateway for:
 * - Debugging and troubleshooting
 * - Performance monitoring
 * - Audit trail
 * 
 * This is especially important during the migration phase to
 * track which requests are being routed to new services vs legacy.
 */
@Slf4j
@Component
public class LoggingFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String requestId = request.getId();
        String method = request.getMethod().name();
        String path = request.getURI().getPath();
        long startTime = Instant.now().toEpochMilli();

        log.info("Gateway Request: [{}] {} {} - Start", requestId, method, path);

        return chain.filter(exchange)
                .then(Mono.fromRunnable(() -> {
                    long duration = Instant.now().toEpochMilli() - startTime;
                    int statusCode = exchange.getResponse().getStatusCode() != null 
                            ? exchange.getResponse().getStatusCode().value() 
                            : 0;
                    log.info("Gateway Response: [{}] {} {} - Status: {} - Duration: {}ms",
                            requestId, method, path, statusCode, duration);
                }));
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
