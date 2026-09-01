package com.carddemo.posttran.domain;

import com.carddemo.posttran.codec.FixedRecord;

import java.math.BigDecimal;

/**
 * {@code TRAN-CAT-BAL-RECORD}, 50 bytes — app/cpy/CVTRA01Y.cpy.
 *
 * <p>This class models the COBOL WORKING-STORAGE group, not a value object, because
 * CBTRN02C reuses one buffer across the whole run and the create path depends on that:
 * {@code INITIALIZE TRAN-CAT-BAL-RECORD} (CBTRN02C.cbl:504) resets the named elementary items but
 * leaves {@code FILLER PIC X(22)} holding whatever the previous {@code READ ... INTO} left there.
 * A newly created category-balance record therefore inherits the filler of the last record read.
 */
public final class TranCatBalRecord extends FixedRecord {

    public static final int LENGTH = 50;

    public TranCatBalRecord() {
        super(LENGTH);
    }

    /** {@code READ TCATBAL-FILE INTO TRAN-CAT-BAL-RECORD} — CBTRN02C.cbl:474. */
    public void readInto(String content) {
        putText(0, LENGTH, content);
    }

    /** {@code INITIALIZE TRAN-CAT-BAL-RECORD} — resets named items only, FILLER is untouched. */
    public void initialize() {
        putUnsigned(0, 11, "0");
        putText(11, 2, "");
        putUnsigned(13, 4, "0");
        putNumber(17, 11, 2, BigDecimal.ZERO);
    }

    public String key() {
        return text(0, 17);
    }

    public void acctId(String value) {
        putUnsigned(0, 11, value);
    }

    public void typeCd(String value) {
        putText(11, 2, value);
    }

    public void catCd(String value) {
        putUnsigned(13, 4, value);
    }

    public BigDecimal balance() {
        return number(17, 11, 2);
    }

    public void addToBalance(BigDecimal amount) {
        putNumber(17, 11, 2, balance().add(amount));
    }
}
