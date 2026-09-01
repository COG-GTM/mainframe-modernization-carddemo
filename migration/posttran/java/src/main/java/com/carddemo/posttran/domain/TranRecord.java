package com.carddemo.posttran.domain;

import com.carddemo.posttran.codec.FixedRecord;

/**
 * {@code TRAN-RECORD}, 350 bytes — app/cpy/CVTRA05Y.cpy.
 *
 * <p>Built field by field from the daily transaction exactly as CBTRN02C.cbl:425-441 does. Like
 * the COBOL working-storage group this buffer is reused across records; every field except
 * {@code FILLER PIC X(20)} is reassigned on each posting, so the filler stays spaces throughout.
 */
public final class TranRecord extends FixedRecord {

    public static final int LENGTH = 350;

    public TranRecord() {
        super(LENGTH);
    }

    /** CBTRN02C.cbl:425-441 — the MOVEs of {@code 2000-POST-TRANSACTION}. */
    public void postFrom(DalytranRecord d, String procTs) {
        putText(0, 16, d.id());
        putText(16, 2, d.typeCd());
        putUnsigned(18, 4, d.catCd());
        putText(22, 10, d.source());
        putText(32, 100, d.desc());
        putNumber(132, 11, 2, d.amount());
        putUnsigned(143, 9, d.merchantId());
        putText(152, 50, d.merchantName());
        putText(202, 50, d.merchantCity());
        putText(252, 10, d.merchantZip());
        putText(262, 16, d.cardNum());
        putText(278, 26, d.origTs());
        putText(304, 26, procTs);
    }

    public String id() {
        return text(0, 16);
    }
}
