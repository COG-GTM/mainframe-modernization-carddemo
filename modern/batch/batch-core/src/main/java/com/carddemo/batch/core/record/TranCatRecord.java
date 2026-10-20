package com.carddemo.batch.core.record;

import com.carddemo.batch.core.CobolDecimal;
import com.carddemo.batch.core.FixedRecord;
import com.carddemo.batch.core.FixedWidth;
import com.carddemo.batch.core.RecordReader;
import com.carddemo.batch.core.RecordWriter;

/**
 * {@code TRAN-CAT-RECORD} from {@code app/cpy/CVTRA04Y.cpy} (RECLN 60), keyed by TRAN-CAT-KEY
 * (type-cd + cat-cd, 6 bytes).
 *
 * <pre>
 * offset len  field
 *      0   2  TRAN-TYPE-CD        X(02)
 *      2   4  TRAN-CAT-CD         9(04)
 *      6  50  TRAN-CAT-TYPE-DESC  X(50)
 *     56   4  FILLER              X(04)
 * </pre>
 */
public record TranCatRecord(String tranTypeCd, int tranCatCd, String tranCatTypeDesc, String filler)
        implements FixedRecord {

    public static final int LENGTH = 60;

    public TranCatRecord {
        tranTypeCd = FixedWidth.fit(tranTypeCd, 2);
        tranCatCd = (int) CobolDecimal.toUnsignedField(tranCatCd, 4);
        tranCatTypeDesc = FixedWidth.fit(tranCatTypeDesc, 50);
        filler = FixedWidth.fit(filler, 4);
    }

    /** The 6-byte TRAN-CAT-KEY. */
    public String key() {
        return key(tranTypeCd, tranCatCd);
    }

    public static String key(String tranTypeCd, int tranCatCd) {
        return FixedWidth.fit(tranTypeCd, 2) + CobolDecimal.formatUnsigned(tranCatCd, 4);
    }

    @Override
    public byte[] toBytes() {
        return new RecordWriter(LENGTH)
                .alpha(tranTypeCd, 2)
                .unsigned(tranCatCd, 4)
                .alpha(tranCatTypeDesc, 50)
                .alpha(filler, 4)
                .toBytes();
    }

    public static TranCatRecord fromBytes(byte[] bytes) {
        RecordReader r = new RecordReader(bytes, LENGTH);
        return new TranCatRecord(r.alpha(2), r.unsignedInt(4), r.alpha(50), r.alpha(4));
    }
}
