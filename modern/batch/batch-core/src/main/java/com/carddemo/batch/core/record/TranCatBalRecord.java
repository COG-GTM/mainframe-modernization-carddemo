package com.carddemo.batch.core.record;

import com.carddemo.batch.core.CobolDecimal;
import com.carddemo.batch.core.FixedRecord;
import com.carddemo.batch.core.FixedWidth;
import com.carddemo.batch.core.RecordReader;
import com.carddemo.batch.core.RecordWriter;
import java.math.BigDecimal;

/**
 * {@code TRAN-CAT-BAL-RECORD} from {@code app/cpy/CVTRA01Y.cpy} (RECLN 50), keyed by
 * TRAN-CAT-KEY (acct-id + type-cd + cat-cd, 17 bytes).
 *
 * <pre>
 * offset len  field
 *      0  11  TRANCAT-ACCT-ID  9(11)
 *     11   2  TRANCAT-TYPE-CD  X(02)
 *     13   4  TRANCAT-CD       9(04)
 *     17  11  TRAN-CAT-BAL     S9(09)V99
 *     28  22  FILLER           X(22)
 * </pre>
 */
public record TranCatBalRecord(long acctId, String typeCd, int catCd, BigDecimal balance, String filler)
        implements FixedRecord {

    public static final int LENGTH = 50;

    public TranCatBalRecord {
        acctId = CobolDecimal.toUnsignedField(acctId, 11);
        typeCd = FixedWidth.fit(typeCd, 2);
        catCd = (int) CobolDecimal.toUnsignedField(catCd, 4);
        balance = CobolDecimal.toField(balance, 9, 2);
        filler = FixedWidth.fit(filler, 22);
    }

    /** A freshly INITIALIZEd record: FILLER is spaces. */
    public TranCatBalRecord(long acctId, String typeCd, int catCd, BigDecimal balance) {
        this(acctId, typeCd, catCd, balance, "");
    }

    /** The 17-byte TRAN-CAT-KEY. */
    public String key() {
        return key(acctId, typeCd, catCd);
    }

    public static String key(long acctId, String typeCd, int catCd) {
        return CobolDecimal.formatUnsigned(acctId, 11) + FixedWidth.fit(typeCd, 2)
                + CobolDecimal.formatUnsigned(catCd, 4);
    }

    public TranCatBalRecord withBalance(BigDecimal value) {
        return new TranCatBalRecord(acctId, typeCd, catCd, value, filler);
    }

    @Override
    public byte[] toBytes() {
        return new RecordWriter(LENGTH)
                .unsigned(acctId, 11)
                .alpha(typeCd, 2)
                .unsigned(catCd, 4)
                .signed(balance, 9, 2)
                .alpha(filler, 22)
                .toBytes();
    }

    public static TranCatBalRecord fromBytes(byte[] bytes) {
        RecordReader r = new RecordReader(bytes, LENGTH);
        return new TranCatBalRecord(r.unsigned(11), r.alpha(2), r.unsignedInt(4), r.signed(9, 2), r.alpha(22));
    }
}
