package com.carddemo.domain;

import com.carddemo.util.CobolCodec;
import com.carddemo.util.FieldCursor;

/**
 * TRAN-CAT-RECORD, copybook CVTRA04Y, LRECL 60 (TRANCATG / trancatg.txt).
 */
public record TransactionCategory(String typeCode, int categoryCode, String description) {

    public static final int RECORD_LENGTH = 60;

    public static TransactionCategory parse(String record) {
        FieldCursor cursor = new FieldCursor(record, RECORD_LENGTH);
        return new TransactionCategory(cursor.fixed(2), cursor.integer(4), cursor.text(50));
    }

    public String format() {
        return CobolCodec.encodeText(typeCode, 2)
                + CobolCodec.encodeNumeric(categoryCode, 4)
                + CobolCodec.encodeText(description, 50)
                + CobolCodec.encodeNumeric(0, 4);
    }

    /** Composite key TRAN-CAT-KEY: transaction type code plus category code. */
    public String key() {
        return typeCode + CobolCodec.encodeNumeric(categoryCode, 4);
    }
}
