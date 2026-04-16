package com.carddemo.shared;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

class BaseEntityTest {

    @Test
    void onCreateSetsTimestamps() {
        TestEntity entity = new TestEntity();
        assertNull(entity.getCreatedAt());
        assertNull(entity.getUpdatedAt());

        entity.onCreate();

        assertNotNull(entity.getCreatedAt());
        assertNotNull(entity.getUpdatedAt());
    }

    @Test
    void onUpdateRefreshesUpdatedAt() {
        TestEntity entity = new TestEntity();
        entity.onCreate();

        var originalUpdatedAt = entity.getUpdatedAt();
        entity.onUpdate();

        assertNotNull(entity.getUpdatedAt());
    }

    /** Concrete subclass for testing the abstract BaseEntity. */
    static class TestEntity extends BaseEntity {
    }
}
