package com.carddemo.posttran.domain;

import com.carddemo.posttran.codec.FixedRecord;

/** {@code CARD-XREF-RECORD}, 50 bytes — app/cpy/CVACT03Y.cpy. Read-only in this step. */
public final class CardXrefRecord extends FixedRecord {

    public static final int LENGTH = 50;

    public CardXrefRecord(String content) {
        super(content, LENGTH);
    }

    public String cardNum() {
        return text(0, 16);
    }

    public String acctId() {
        return text(25, 11);
    }
}
