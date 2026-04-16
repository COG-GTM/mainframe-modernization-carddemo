package com.cardemo.monitoring;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

/**
 * API request latency histogram by endpoint.
 */
@Component
public class ApiLatencyMetrics {

    private final MeterRegistry registry;
    private final ConcurrentMap<String, Timer> endpointTimers = new ConcurrentHashMap<>();

    public ApiLatencyMetrics(MeterRegistry registry) {
        this.registry = registry;
    }

    public Timer.Sample startTimer() {
        return Timer.start(registry);
    }

    public void recordLatency(Timer.Sample sample, String endpoint, String method) {
        Timer timer = endpointTimers.computeIfAbsent(
                method + ":" + endpoint,
                key -> Timer.builder("api_request_latency_seconds")
                        .description("API request latency by endpoint")
                        .tag("endpoint", endpoint)
                        .tag("method", method)
                        .publishPercentileHistogram()
                        .minimumExpectedValue(Duration.ofMillis(1))
                        .maximumExpectedValue(Duration.ofSeconds(30))
                        .register(registry)
        );
        sample.stop(timer);
    }

    public void recordLatency(String endpoint, String method, long durationMs) {
        Timer timer = endpointTimers.computeIfAbsent(
                method + ":" + endpoint,
                key -> Timer.builder("api_request_latency_seconds")
                        .description("API request latency by endpoint")
                        .tag("endpoint", endpoint)
                        .tag("method", method)
                        .publishPercentileHistogram()
                        .minimumExpectedValue(Duration.ofMillis(1))
                        .maximumExpectedValue(Duration.ofSeconds(30))
                        .register(registry)
        );
        timer.record(durationMs, TimeUnit.MILLISECONDS);
    }

    public Timer getTimer(String endpoint, String method) {
        return endpointTimers.get(method + ":" + endpoint);
    }
}
