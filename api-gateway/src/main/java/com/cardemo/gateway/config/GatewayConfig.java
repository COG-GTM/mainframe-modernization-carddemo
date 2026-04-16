package com.cardemo.gateway.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * Gateway configuration for downstream service URLs.
 * Maps COBOL program names to their modernized service base URLs.
 */
@Configuration
@ConfigurationProperties(prefix = "gateway")
public class GatewayConfig {

    private Map<String, String> services = new HashMap<>();

    public Map<String, String> getServices() {
        return services;
    }

    public void setServices(Map<String, String> services) {
        this.services = services;
    }

    public String getServiceUrl(String serviceName) {
        return services.getOrDefault(serviceName, "http://localhost:8080");
    }
}
