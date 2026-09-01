package com.carddemo.posttran.domain;

import com.carddemo.posttran.codec.FixedRecord;

import java.math.BigDecimal;

/** {@code DALYTRAN-RECORD}, 350 bytes — app/cpy/CVTRA06Y.cpy. */
public final class DalytranRecord extends FixedRecord {

    public static final int LENGTH = 350;

    public DalytranRecord(String content) {
        super(content, LENGTH);
    }

    public String id() {
        return text(0, 16);
    }

    public String typeCd() {
        return text(16, 2);
    }

    public String catCd() {
        return text(18, 4);
    }

    public String source() {
        return text(22, 10);
    }

    public String desc() {
        return text(32, 100);
    }

    public BigDecimal amount() {
        return number(132, 11, 2);
    }

    public String merchantId() {
        return text(143, 9);
    }

    public String merchantName() {
        return text(152, 50);
    }

    public String merchantCity() {
        return text(202, 50);
    }

    public String merchantZip() {
        return text(252, 10);
    }

    public String cardNum() {
        return text(262, 16);
    }

    public String origTs() {
        return text(278, 26);
    }

    /** {@code DALYTRAN-ORIG-TS (1:10)} — the date part used by the expiry check, CBTRN02C.cbl:414. */
    public String origDate() {
        return text(278, 10);
    }
}
