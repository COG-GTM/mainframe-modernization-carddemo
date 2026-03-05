package com.carddemo.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Wait utility — replaces MVSWAIT assembler utility.
 * Uses Thread.sleep() or ScheduledExecutorService for delays.
 */
public final class WaitUtils {

    private static final Logger log = LoggerFactory.getLogger(WaitUtils.class);
    private static final ScheduledExecutorService SCHEDULER = Executors.newSingleThreadScheduledExecutor();

    private WaitUtils() {}

    /**
     * Wait for the specified number of milliseconds.
     * Replaces MVSWAIT assembler macro.
     */
    public static void waitMillis(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("Wait interrupted");
        }
    }

    /**
     * Wait for the specified number of seconds.
     */
    public static void waitSeconds(int seconds) {
        waitMillis(seconds * 1000L);
    }

    /**
     * Schedule a task to execute after a delay.
     */
    public static void scheduleAfter(Runnable task, long delayMillis) {
        SCHEDULER.schedule(task, delayMillis, TimeUnit.MILLISECONDS);
    }
}
