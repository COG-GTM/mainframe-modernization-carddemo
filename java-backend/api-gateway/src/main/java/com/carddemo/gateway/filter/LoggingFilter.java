package com.carddemo.gateway.filter;

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
 * Global Logging Filter
 * 
 * Logs all requests passing through the gateway for monitoring and debugging.
 * 
 * In the mainframe, transaction logging was handled by CICS SMF records.
 * This filter provides similar visibility for the modernized system.
 * 
 * Logged information:
 * - Request method and path
 * - Client IP address
 * - Request timestamp
 * - Response status and duration
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
        String clientIp = request.getRemoteAddress() != null 
                ? request.getRemoteAddress().getAddress().getHostAddress() 
                : "unknown";
        
        long startTime = Instant.now().toEpochMilli();

        log.info("Gateway Request: id={}, method={}, path={}, clientIp={}", 
                requestId, method, path, clientIp);

        return chain.filter(exchange)
                .then(Mono.fromRunnable(() -> {
                    long duration = Instant.now().toEpochMilli() - startTime;
                    int statusCode = exchange.getResponse().getStatusCode() != null 
                            ? exchange.getResponse().getStatusCode().value() 
                            : 0;
                    
                    log.info("Gateway Response: id={}, status={}, duration={}ms", 
                            requestId, statusCode, duration);
                }));
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
