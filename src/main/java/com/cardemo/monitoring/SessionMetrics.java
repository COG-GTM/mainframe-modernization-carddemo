package com.cardemo.monitoring;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Active user session gauge metric.
 */
@Component
public class SessionMetrics {

    private final AtomicInteger activeSessions = new AtomicInteger(0);

    public SessionMetrics(MeterRegistry registry) {
        Gauge.builder("active_user_sessions", activeSessions, AtomicInteger::get)
                .description("Number of currently active user sessions")
                .register(registry);
    }

    public void sessionCreated() {
        activeSessions.incrementAndGet();
    }

    public void sessionDestroyed() {
        activeSessions.decrementAndGet();
    }

    public int getActiveSessions() {
        return activeSessions.get();
    }
}
