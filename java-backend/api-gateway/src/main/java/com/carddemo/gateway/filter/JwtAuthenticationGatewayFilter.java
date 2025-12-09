package com.carddemo.gateway.filter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
public class JwtAuthenticationGatewayFilter implements GlobalFilter, Ordered {

    @Value("${gateway.auth.excluded-paths:/api/v1/auth/login,/api/v1/auth/register,/actuator/**}")
    private List<String> excludedPaths;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getPath().value();

        if (isExcludedPath(path)) {
            return chain.filter(exchange);
        }

        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        return chain.filter(exchange);
    }

    private boolean isExcludedPath(String path) {
        return excludedPaths.stream().anyMatch(excluded -> {
            if (excluded.endsWith("/**")) {
                String prefix = excluded.substring(0, excluded.length() - 3);
                return path.startsWith(prefix);
            }
            return path.equals(excluded);
        });
    }

    @Override
    public int getOrder() {
        return -100;
    }
}
