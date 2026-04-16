package com.cardemo.monitoring;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Database connection pool metrics.
 * Tracks active, idle, and total connections.
 */
@Component
public class DatabasePoolMetrics {

    private final AtomicInteger activeConnections = new AtomicInteger(0);
    private final AtomicInteger idleConnections = new AtomicInteger(0);
    private final AtomicInteger totalConnections = new AtomicInteger(0);

    public DatabasePoolMetrics(MeterRegistry registry) {
        Gauge.builder("db_pool_active_connections", activeConnections, AtomicInteger::get)
                .description("Number of active database connections")
                .register(registry);

        Gauge.builder("db_pool_idle_connections", idleConnections, AtomicInteger::get)
                .description("Number of idle database connections")
                .register(registry);

        Gauge.builder("db_pool_total_connections", totalConnections, AtomicInteger::get)
                .description("Total number of database connections in pool")
                .register(registry);
    }

    public void updateActiveConnections(int count) {
        activeConnections.set(count);
    }

    public void updateIdleConnections(int count) {
        idleConnections.set(count);
    }

    public void updateTotalConnections(int count) {
        totalConnections.set(count);
    }

    public int getActiveConnections() {
        return activeConnections.get();
    }

    public int getIdleConnections() {
        return idleConnections.get();
    }

    public int getTotalConnections() {
        return totalConnections.get();
    }
}
