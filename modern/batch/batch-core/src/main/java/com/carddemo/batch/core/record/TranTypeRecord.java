package com.carddemo.batch.core.record;

import com.carddemo.batch.core.FixedRecord;
import com.carddemo.batch.core.FixedWidth;
import com.carddemo.batch.core.RecordReader;
import com.carddemo.batch.core.RecordWriter;

/**
 * {@code TRAN-TYPE-RECORD} from {@code app/cpy/CVTRA03Y.cpy} (RECLN 60), keyed by TRAN-TYPE.
 *
 * <pre>
 * offset len  field
 *      0   2  TRAN-TYPE       X(02)
 *      2  50  TRAN-TYPE-DESC  X(50)
 *     52   8  FILLER          X(08)
 * </pre>
 */
public record TranTypeRecord(String tranType, String tranTypeDesc, String filler) implements FixedRecord {

    public static final int LENGTH = 60;

    public TranTypeRecord {
        tranType = FixedWidth.fit(tranType, 2);
        tranTypeDesc = FixedWidth.fit(tranTypeDesc, 50);
        filler = FixedWidth.fit(filler, 8);
    }

    @Override
    public byte[] toBytes() {
        return new RecordWriter(LENGTH).alpha(tranType, 2).alpha(tranTypeDesc, 50).alpha(filler, 8).toBytes();
    }

    public static TranTypeRecord fromBytes(byte[] bytes) {
        RecordReader r = new RecordReader(bytes, LENGTH);
        return new TranTypeRecord(r.alpha(2), r.alpha(50), r.alpha(8));
    }
}
