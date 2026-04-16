package com.cardemo.monitoring;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SessionMetricsTest {

    private MeterRegistry registry;
    private SessionMetrics metrics;

    @BeforeEach
    void setUp() {
        registry = new SimpleMeterRegistry();
        metrics = new SessionMetrics(registry);
    }

    @Test
    void activeSessionsStartsAtZero() {
        assertEquals(0, metrics.getActiveSessions());
    }

    @Test
    void sessionCreatedIncrementsGauge() {
        metrics.sessionCreated();
        metrics.sessionCreated();

        assertEquals(2, metrics.getActiveSessions());
    }

    @Test
    void sessionDestroyedDecrementsGauge() {
        metrics.sessionCreated();
        metrics.sessionCreated();
        metrics.sessionDestroyed();

        assertEquals(1, metrics.getActiveSessions());
    }

    @Test
    void gaugeRegisteredInRegistry() {
        metrics.sessionCreated();

        var gauge = registry.find("active_user_sessions").gauge();
        assert gauge != null;
        assertEquals(1.0, gauge.value());
    }
}
