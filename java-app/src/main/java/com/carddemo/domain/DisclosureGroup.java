package com.carddemo.domain;

import com.carddemo.util.CobolCodec;
import com.carddemo.util.FieldCursor;

import java.math.BigDecimal;

/**
 * DIS-GROUP-RECORD, copybook CVTRA02Y, LRECL 50 (DISCGRP / discgrp.txt).
 *
 * <p>Holds the annual interest rate applied by the interest calculation batch job (CBACT04C) for a
 * given account group, transaction type and transaction category.
 */
public record DisclosureGroup(String accountGroupId, String typeCode, int categoryCode, BigDecimal interestRate) {

    public static final int RECORD_LENGTH = 50;

    public static DisclosureGroup parse(String record) {
        FieldCursor cursor = new FieldCursor(record, RECORD_LENGTH);
        return new DisclosureGroup(cursor.text(10), cursor.fixed(2), cursor.integer(4), cursor.signed(6, 2));
    }

    public String format() {
        return CobolCodec.encodeText(accountGroupId, 10)
                + CobolCodec.encodeText(typeCode, 2)
                + CobolCodec.encodeNumeric(categoryCode, 4)
                + CobolCodec.encodeSigned(interestRate, 6, 2)
                + " ".repeat(28);
    }

    /** Composite key DIS-GROUP-KEY. */
    public String key() {
        return CobolCodec.encodeText(accountGroupId, 10) + typeCode + CobolCodec.encodeNumeric(categoryCode, 4);
    }
}
