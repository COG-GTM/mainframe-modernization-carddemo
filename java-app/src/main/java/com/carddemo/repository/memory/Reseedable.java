package com.carddemo.repository.memory;

/**
 * Implemented by the in-memory repositories so tests can restore the canonical mock dataset between
 * cases that mutate balances or user records.
 */
public interface Reseedable {

    void reseed();
}
