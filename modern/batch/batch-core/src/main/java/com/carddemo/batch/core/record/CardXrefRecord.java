package com.carddemo.batch.core.record;

import com.carddemo.batch.core.CobolDecimal;
import com.carddemo.batch.core.FixedRecord;
import com.carddemo.batch.core.FixedWidth;
import com.carddemo.batch.core.RecordReader;
import com.carddemo.batch.core.RecordWriter;

/**
 * {@code CARD-XREF-RECORD} from {@code app/cpy/CVACT03Y.cpy} (RECLN 50), keyed by XREF-CARD-NUM
 * with alternate key XREF-ACCT-ID.
 *
 * <pre>
 * offset len  field
 *      0  16  XREF-CARD-NUM  X(16)
 *     16   9  XREF-CUST-ID   9(09)
 *     25  11  XREF-ACCT-ID   9(11)
 *     36  14  FILLER         X(14)
 * </pre>
 */
public record CardXrefRecord(String cardNum, long custId, long acctId, String filler) implements FixedRecord {

    public static final int LENGTH = 50;

    public CardXrefRecord {
        cardNum = FixedWidth.fit(cardNum, 16);
        custId = CobolDecimal.toUnsignedField(custId, 9);
        acctId = CobolDecimal.toUnsignedField(acctId, 11);
        filler = FixedWidth.fit(filler, 14);
    }

    @Override
    public byte[] toBytes() {
        return new RecordWriter(LENGTH)
                .alpha(cardNum, 16)
                .unsigned(custId, 9)
                .unsigned(acctId, 11)
                .alpha(filler, 14)
                .toBytes();
    }

    public static CardXrefRecord fromBytes(byte[] bytes) {
        RecordReader r = new RecordReader(bytes, LENGTH);
        return new CardXrefRecord(r.alpha(16), r.unsigned(9), r.unsigned(11), r.alpha(14));
    }
}
