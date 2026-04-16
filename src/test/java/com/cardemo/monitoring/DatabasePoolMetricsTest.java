package com.cardemo.monitoring;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class DatabasePoolMetricsTest {

    private MeterRegistry registry;
    private DatabasePoolMetrics metrics;

    @BeforeEach
    void setUp() {
        registry = new SimpleMeterRegistry();
        metrics = new DatabasePoolMetrics(registry);
    }

    @Test
    void activeConnectionsUpdatable() {
        metrics.updateActiveConnections(5);

        assertEquals(5, metrics.getActiveConnections());
    }

    @Test
    void idleConnectionsUpdatable() {
        metrics.updateIdleConnections(10);

        assertEquals(10, metrics.getIdleConnections());
    }

    @Test
    void totalConnectionsUpdatable() {
        metrics.updateTotalConnections(15);

        assertEquals(15, metrics.getTotalConnections());
    }

    @Test
    void gaugesRegisteredInRegistry() {
        metrics.updateActiveConnections(3);
        metrics.updateIdleConnections(7);
        metrics.updateTotalConnections(10);

        var active = registry.find("db_pool_active_connections").gauge();
        var idle = registry.find("db_pool_idle_connections").gauge();
        var total = registry.find("db_pool_total_connections").gauge();

        assertNotNull(active);
        assertNotNull(idle);
        assertNotNull(total);

        assertEquals(3.0, active.value());
        assertEquals(7.0, idle.value());
        assertEquals(10.0, total.value());
    }
}
