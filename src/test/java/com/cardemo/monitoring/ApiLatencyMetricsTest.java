package com.cardemo.monitoring;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ApiLatencyMetricsTest {

    private MeterRegistry registry;
    private ApiLatencyMetrics metrics;

    @BeforeEach
    void setUp() {
        registry = new SimpleMeterRegistry();
        metrics = new ApiLatencyMetrics(registry);
    }

    @Test
    void recordLatencyByEndpoint() {
        metrics.recordLatency("/api/accounts", "GET", 150);

        var timer = metrics.getTimer("/api/accounts", "GET");
        assertNotNull(timer);
        assertEquals(1, timer.count());
    }

    @Test
    void recordLatencyWithTimerSample() throws InterruptedException {
        var sample = metrics.startTimer();
        Thread.sleep(50);
        metrics.recordLatency(sample, "/api/transactions", "POST");

        var timer = metrics.getTimer("/api/transactions", "POST");
        assertNotNull(timer);
        assertEquals(1, timer.count());
        assertTrue(timer.mean(java.util.concurrent.TimeUnit.MILLISECONDS) >= 40);
    }

    @Test
    void differentEndpointsHaveSeparateTimers() {
        metrics.recordLatency("/api/accounts", "GET", 100);
        metrics.recordLatency("/api/transactions", "POST", 200);

        var accountTimer = metrics.getTimer("/api/accounts", "GET");
        var txnTimer = metrics.getTimer("/api/transactions", "POST");

        assertNotNull(accountTimer);
        assertNotNull(txnTimer);
        assertEquals(1, accountTimer.count());
        assertEquals(1, txnTimer.count());
    }

    @Test
    void nonexistentTimerReturnsNull() {
        assertNull(metrics.getTimer("/api/nonexistent", "GET"));
    }
}
