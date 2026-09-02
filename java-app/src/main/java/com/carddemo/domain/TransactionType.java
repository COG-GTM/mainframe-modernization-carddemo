package com.carddemo.domain;

import com.carddemo.util.CobolCodec;
import com.carddemo.util.FieldCursor;

/**
 * TRAN-TYPE-RECORD, copybook CVTRA03Y, LRECL 60 (TRANTYPE / trantype.txt).
 */
public record TransactionType(String typeCode, String description) {

    public static final int RECORD_LENGTH = 60;

    public static TransactionType parse(String record) {
        FieldCursor cursor = new FieldCursor(record, RECORD_LENGTH);
        return new TransactionType(cursor.fixed(2), cursor.text(50));
    }

    public String format() {
        return CobolCodec.encodeText(typeCode, 2)
                + CobolCodec.encodeText(description, 50)
                + CobolCodec.encodeNumeric(0, 8);
    }
}
