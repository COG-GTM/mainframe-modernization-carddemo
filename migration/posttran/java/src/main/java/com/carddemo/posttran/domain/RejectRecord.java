package com.carddemo.posttran.domain;

import com.carddemo.posttran.codec.FixedRecord;

/**
 * {@code REJECT-RECORD}, 430 bytes — CBTRN02C.cbl:176-183 (WORKING-STORAGE, not a copybook):
 * the untouched 350-byte daily transaction followed by an 80-byte validation trailer of
 * {@code PIC 9(04)} reason code and {@code PIC X(76)} description.
 *
 * <p>430 is the {@code LRECL} the JCL declares for DALYREJS (POSTTRAN.jcl:33-36).
 */
public final class RejectRecord extends FixedRecord {

    public static final int LENGTH = 430;

    public RejectRecord(DalytranRecord tran, int reason, String description) {
        super(LENGTH);
        putText(0, 350, tran.toString());
        putUnsigned(350, 4, Integer.toString(reason));
        putText(354, 76, description);
    }
}
