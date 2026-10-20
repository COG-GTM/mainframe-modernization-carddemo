package com.carddemo.batch.core.report;

import com.carddemo.batch.core.FixedRecord;
import com.carddemo.batch.core.FixedWidth;
import com.carddemo.batch.core.RecordReader;
import com.carddemo.batch.core.RecordWriter;

/**
 * {@code REPORT-NAME-HEADER} from {@code app/cpy/CVTRA07Y.cpy}, padded to 133 bytes.
 *
 * <pre>
 * offset len  field
 *      0  38  REPT-SHORT-NAME   'DALYREPT'
 *     38  41  REPT-LONG-NAME    'Daily Transaction Report'
 *     79  12  REPT-DATE-HEADER  'Date Range: '
 *     91  10  REPT-START-DATE
 *    101   4  FILLER            ' to '
 *    105  10  REPT-END-DATE
 * </pre>
 */
public record ReportNameHeader(String startDate, String endDate) implements FixedRecord {

    public ReportNameHeader {
        startDate = FixedWidth.fit(startDate, 10);
        endDate = FixedWidth.fit(endDate, 10);
    }

    @Override
    public byte[] toBytes() {
        return new RecordWriter(ReportLines.LENGTH)
                .alpha("DALYREPT", 38)
                .alpha("Daily Transaction Report", 41)
                .alpha("Date Range: ", 12)
                .alpha(startDate, 10)
                .alpha(" to ", 4)
                .alpha(endDate, 10)
                .toBytes();
    }

    public static ReportNameHeader fromBytes(byte[] bytes) {
        RecordReader r = new RecordReader(ReportLines.requireLength(bytes), ReportLines.LENGTH).seek(91);
        String start = r.alpha(10);
        r.skip(4);
        return new ReportNameHeader(start, r.alpha(10));
    }
}
