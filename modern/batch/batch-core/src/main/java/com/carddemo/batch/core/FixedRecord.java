package com.carddemo.batch.core;

/** A record with a fixed-length byte image (the copybook layout). */
public interface FixedRecord {

    byte[] toBytes();
}
