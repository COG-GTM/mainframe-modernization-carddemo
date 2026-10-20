package com.carddemo.batch.core.record;

import com.carddemo.batch.core.CobolDecimal;
import com.carddemo.batch.core.FixedRecord;
import com.carddemo.batch.core.FixedWidth;
import com.carddemo.batch.core.RecordReader;
import com.carddemo.batch.core.RecordWriter;
import java.util.Arrays;

/**
 * {@code FD-REJS-RECORD} of {@code DALYREJS-FILE} in {@code app/cbl/CBTRN02C.cbl} (430 bytes):
 * the rejected DALYTRAN record followed by WS-VALIDATION-TRAILER.
 *
 * <pre>
 * offset len  field
 *      0 350  REJECT-TRAN-DATA                X(350)  (DALYTRAN-RECORD)
 *    350   4  WS-VALIDATION-FAIL-REASON       9(04)
 *    354  76  WS-VALIDATION-FAIL-REASON-DESC  X(76)
 * </pre>
 */
public record RejectRecord(DailyTranRecord tran, int validationFailReason, String validationFailReasonDesc)
        implements FixedRecord {

    public static final int LENGTH = 430;
    public static final int TRAILER_LENGTH = 80;

    public RejectRecord {
        validationFailReason = (int) CobolDecimal.toUnsignedField(validationFailReason, 4);
        validationFailReasonDesc = FixedWidth.fit(validationFailReasonDesc, 76);
    }

    @Override
    public byte[] toBytes() {
        byte[] trailer = new RecordWriter(TRAILER_LENGTH)
                .unsigned(validationFailReason, 4)
                .alpha(validationFailReasonDesc, 76)
                .toBytes();
        byte[] out = Arrays.copyOf(tran.toBytes(), LENGTH);
        System.arraycopy(trailer, 0, out, DailyTranRecord.LENGTH, TRAILER_LENGTH);
        return out;
    }

    public static RejectRecord fromBytes(byte[] bytes) {
        if (bytes.length != LENGTH) {
            throw new IllegalArgumentException("Expected a " + LENGTH + "-byte record but got " + bytes.length);
        }
        DailyTranRecord tran = DailyTranRecord.fromBytes(Arrays.copyOfRange(bytes, 0, DailyTranRecord.LENGTH));
        RecordReader trailer = new RecordReader(Arrays.copyOfRange(bytes, DailyTranRecord.LENGTH, LENGTH), TRAILER_LENGTH);
        return new RejectRecord(tran, trailer.unsignedInt(4), trailer.alpha(76));
    }
}
