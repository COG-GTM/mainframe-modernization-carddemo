package com.carddemo.batch.core.record;

import com.carddemo.batch.core.CobolDecimal;
import com.carddemo.batch.core.FixedRecord;
import com.carddemo.batch.core.FixedWidth;
import com.carddemo.batch.core.RecordReader;
import com.carddemo.batch.core.RecordWriter;
import java.math.BigDecimal;

/**
 * {@code DIS-GROUP-RECORD} from {@code app/cpy/CVTRA02Y.cpy} (RECLN 50), keyed by DIS-GROUP-KEY
 * (group-id + type-cd + cat-cd, 16 bytes).
 *
 * <pre>
 * offset len  field
 *      0  10  DIS-ACCT-GROUP-ID  X(10)
 *     10   2  DIS-TRAN-TYPE-CD   X(02)
 *     12   4  DIS-TRAN-CAT-CD    9(04)
 *     16   6  DIS-INT-RATE       S9(04)V99
 *     22  28  FILLER             X(28)
 * </pre>
 */
public record DiscGroupRecord(String acctGroupId, String tranTypeCd, int tranCatCd, BigDecimal intRate,
        String filler) implements FixedRecord {

    public static final int LENGTH = 50;
    public static final String DEFAULT_GROUP = "DEFAULT";

    public DiscGroupRecord {
        acctGroupId = FixedWidth.fit(acctGroupId, 10);
        tranTypeCd = FixedWidth.fit(tranTypeCd, 2);
        tranCatCd = (int) CobolDecimal.toUnsignedField(tranCatCd, 4);
        intRate = CobolDecimal.toField(intRate, 4, 2);
        filler = FixedWidth.fit(filler, 28);
    }

    /** The 16-byte DIS-GROUP-KEY. */
    public String key() {
        return key(acctGroupId, tranTypeCd, tranCatCd);
    }

    public static String key(String acctGroupId, String tranTypeCd, int tranCatCd) {
        return FixedWidth.fit(acctGroupId, 10) + FixedWidth.fit(tranTypeCd, 2)
                + CobolDecimal.formatUnsigned(tranCatCd, 4);
    }

    @Override
    public byte[] toBytes() {
        return new RecordWriter(LENGTH)
                .alpha(acctGroupId, 10)
                .alpha(tranTypeCd, 2)
                .unsigned(tranCatCd, 4)
                .signed(intRate, 4, 2)
                .alpha(filler, 28)
                .toBytes();
    }

    public static DiscGroupRecord fromBytes(byte[] bytes) {
        RecordReader r = new RecordReader(bytes, LENGTH);
        return new DiscGroupRecord(r.alpha(10), r.alpha(2), r.unsignedInt(4), r.signed(4, 2), r.alpha(28));
    }
}
