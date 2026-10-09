package com.carddemo.service;

import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Component;

/**
 * Allocates the next 16 digit TRAN-ID (the COBOL programs read TRANSACT backwards from HIGH-VALUES
 * and added one). Shared by every writer so ids are unique within the JVM; pending inserts are
 * flushed first and the daily file is included so a later POSTTRAN cannot reuse an allocated id.
 */
@Component
public class TransactionIdGenerator {

    private final EntityManager entityManager;

    public TransactionIdGenerator(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public synchronized String next() {
        entityManager.flush();
        long last = Math.max(
                max("select max(t.id) from Transaction t"),
                max("select max(d.id) from DailyTransaction d"));
        return String.format("%016d", last + 1);
    }

    private long max(String query) {
        String id = entityManager.createQuery(query, String.class).getSingleResult();
        return id == null || id.isBlank() ? 0L : Long.parseLong(id.trim());
    }
}
