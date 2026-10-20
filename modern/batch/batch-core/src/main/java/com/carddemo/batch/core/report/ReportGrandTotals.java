package com.carddemo.batch.core.report;

import com.carddemo.batch.core.CobolDecimal;
import com.carddemo.batch.core.FixedRecord;
import com.carddemo.batch.core.RecordReader;
import com.carddemo.batch.core.RecordWriter;
import java.math.BigDecimal;

/**
 * {@code REPORT-GRAND-TOTALS} from {@code app/cpy/CVTRA07Y.cpy}, padded to 133 bytes: 'Grand Total' in X(11), 86 dots,
 * then {@code REPT-GRAND-TOTAL} as {@code +ZZZ,ZZZ,ZZZ.ZZ} at offset 97.
 */
public record ReportGrandTotals(BigDecimal total) implements FixedRecord {

    public static final String LABEL = "Grand Total";

    public ReportGrandTotals {
        total = CobolDecimal.toField(total, 9, 2);
    }

    @Override
    public byte[] toBytes() {
        return new RecordWriter(ReportLines.LENGTH)
                .alpha(LABEL, 11)
                .alpha(".".repeat(86), 86)
                .edited(total, ReportLines.TOTAL_PICTURE)
                .toBytes();
    }

    public static ReportGrandTotals fromBytes(byte[] bytes) {
        return new ReportGrandTotals(new RecordReader(ReportLines.requireLength(bytes), ReportLines.LENGTH).seek(97).edited(15, 2));
    }
}
