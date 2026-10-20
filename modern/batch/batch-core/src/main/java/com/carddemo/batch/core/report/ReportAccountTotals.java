package com.carddemo.batch.core.report;

import com.carddemo.batch.core.CobolDecimal;
import com.carddemo.batch.core.FixedRecord;
import com.carddemo.batch.core.RecordReader;
import com.carddemo.batch.core.RecordWriter;
import java.math.BigDecimal;

/**
 * {@code REPORT-ACCOUNT-TOTALS} from {@code app/cpy/CVTRA07Y.cpy}, padded to 133 bytes: 'Account Total' in X(13), 84 dots,
 * then {@code REPT-ACCOUNT-TOTAL} as {@code +ZZZ,ZZZ,ZZZ.ZZ} at offset 97.
 */
public record ReportAccountTotals(BigDecimal total) implements FixedRecord {

    public static final String LABEL = "Account Total";

    public ReportAccountTotals {
        total = CobolDecimal.toField(total, 9, 2);
    }

    @Override
    public byte[] toBytes() {
        return new RecordWriter(ReportLines.LENGTH)
                .alpha(LABEL, 13)
                .alpha(".".repeat(84), 84)
                .edited(total, ReportLines.TOTAL_PICTURE)
                .toBytes();
    }

    public static ReportAccountTotals fromBytes(byte[] bytes) {
        return new ReportAccountTotals(new RecordReader(ReportLines.requireLength(bytes), ReportLines.LENGTH).seek(97).edited(15, 2));
    }
}
