package com.carddemo.batch.core.report;

import com.carddemo.batch.core.CobolDecimal;
import com.carddemo.batch.core.FixedRecord;
import com.carddemo.batch.core.FixedWidth;
import com.carddemo.batch.core.RecordReader;
import com.carddemo.batch.core.RecordWriter;
import java.math.BigDecimal;

/**
 * {@code TRANSACTION-DETAIL-REPORT} from {@code app/cpy/CVTRA07Y.cpy}, padded to 133 bytes.
 *
 * <pre>
 * offset len  field
 *      0  16  TRAN-REPORT-TRANS-ID    X(16)
 *     16   1  FILLER                  ' '
 *     17  11  TRAN-REPORT-ACCOUNT-ID  X(11)
 *     28   1  FILLER                  ' '
 *     29   2  TRAN-REPORT-TYPE-CD     X(02)
 *     31   1  FILLER                  '-'
 *     32  15  TRAN-REPORT-TYPE-DESC   X(15)
 *     47   1  FILLER                  ' '
 *     48   4  TRAN-REPORT-CAT-CD      9(04)
 *     52   1  FILLER                  '-'
 *     53  29  TRAN-REPORT-CAT-DESC    X(29)
 *     82   1  FILLER                  ' '
 *     83  10  TRAN-REPORT-SOURCE      X(10)
 *     93   4  FILLER                  spaces
 *     97  15  TRAN-REPORT-AMT         -ZZZ,ZZZ,ZZZ.ZZ
 *    112   2  FILLER                  spaces
 * </pre>
 */
public record TransactionDetailLine(
        String transId,
        String accountId,
        String typeCd,
        String typeDesc,
        int catCd,
        String catDesc,
        String source,
        BigDecimal amount) implements FixedRecord {

    public TransactionDetailLine {
        transId = FixedWidth.fit(transId, 16);
        accountId = FixedWidth.fit(accountId, 11);
        typeCd = FixedWidth.fit(typeCd, 2);
        typeDesc = FixedWidth.fit(typeDesc, 15);
        catCd = (int) CobolDecimal.toUnsignedField(catCd, 4);
        catDesc = FixedWidth.fit(catDesc, 29);
        source = FixedWidth.fit(source, 10);
        amount = CobolDecimal.toField(amount, 9, 2);
    }

    @Override
    public byte[] toBytes() {
        return new RecordWriter(ReportLines.LENGTH)
                .alpha(transId, 16)
                .alpha(" ", 1)
                .alpha(accountId, 11)
                .alpha(" ", 1)
                .alpha(typeCd, 2)
                .alpha("-", 1)
                .alpha(typeDesc, 15)
                .alpha(" ", 1)
                .unsigned(catCd, 4)
                .alpha("-", 1)
                .alpha(catDesc, 29)
                .alpha(" ", 1)
                .alpha(source, 10)
                .alpha("", 4)
                .edited(amount, ReportLines.AMOUNT_PICTURE)
                .alpha("", 2)
                .toBytes();
    }

    public static TransactionDetailLine fromBytes(byte[] bytes) {
        RecordReader r = new RecordReader(ReportLines.requireLength(bytes), ReportLines.LENGTH);
        String transId = r.alpha(16);
        String accountId = r.skip(1).alpha(11);
        String typeCd = r.skip(1).alpha(2);
        String typeDesc = r.skip(1).alpha(15);
        int catCd = r.skip(1).unsignedInt(4);
        String catDesc = r.skip(1).alpha(29);
        String source = r.skip(1).alpha(10);
        BigDecimal amount = r.skip(4).edited(15, 2);
        return new TransactionDetailLine(transId, accountId, typeCd, typeDesc, catCd, catDesc, source, amount);
    }
}
