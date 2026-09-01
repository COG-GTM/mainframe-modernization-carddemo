package com.carddemo.posttran.domain;

import com.carddemo.posttran.codec.FixedRecord;

import java.math.BigDecimal;

/**
 * {@code ACCOUNT-RECORD}, 300 bytes — app/cpy/CVACT01Y.cpy.
 *
 * <p>FILLER is deliberately retained rather than dropped: this record is rewritten in place
 * (CBTRN02C.cbl:554) and the parity diff compares the resulting after-image byte for byte.
 */
public final class AccountRecord extends FixedRecord {

    public static final int LENGTH = 300;

    public AccountRecord(String content) {
        super(content, LENGTH);
    }

    public String acctId() {
        return text(0, 11);
    }

    public BigDecimal currBal() {
        return number(12, 12, 2);
    }

    public BigDecimal creditLimit() {
        return number(24, 12, 2);
    }

    public String expirationDate() {
        return text(58, 10);
    }

    public BigDecimal currCycCredit() {
        return number(78, 12, 2);
    }

    public BigDecimal currCycDebit() {
        return number(90, 12, 2);
    }

    public void currBal(BigDecimal value) {
        putNumber(12, 12, 2, value);
    }

    public void currCycCredit(BigDecimal value) {
        putNumber(78, 12, 2, value);
    }

    public void currCycDebit(BigDecimal value) {
        putNumber(90, 12, 2, value);
    }
}
