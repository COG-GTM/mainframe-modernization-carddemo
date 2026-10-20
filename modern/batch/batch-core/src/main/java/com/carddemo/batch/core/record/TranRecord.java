package com.carddemo.batch.core.record;

import com.carddemo.batch.core.CobolDecimal;
import com.carddemo.batch.core.FixedRecord;
import com.carddemo.batch.core.FixedWidth;
import com.carddemo.batch.core.RecordReader;
import com.carddemo.batch.core.RecordWriter;
import java.math.BigDecimal;

/**
 * {@code TRAN-RECORD} from {@code app/cpy/CVTRA05Y.cpy} (RECLN 350).
 *
 * <pre>
 * offset len  field
 *      0  16  TRAN-ID              X(16)
 *     16   2  TRAN-TYPE-CD         X(02)
 *     18   4  TRAN-CAT-CD          9(04)
 *     22  10  TRAN-SOURCE          X(10)
 *     32 100  TRAN-DESC            X(100)
 *    132  11  TRAN-AMT             S9(09)V99
 *    143   9  TRAN-MERCHANT-ID     9(09)
 *    152  50  TRAN-MERCHANT-NAME   X(50)
 *    202  50  TRAN-MERCHANT-CITY   X(50)
 *    252  10  TRAN-MERCHANT-ZIP    X(10)
 *    262  16  TRAN-CARD-NUM        X(16)
 *    278  26  TRAN-ORIG-TS         X(26)
 *    304  26  TRAN-PROC-TS         X(26)
 *    330  20  FILLER             X(20)
 * </pre>
 *
 * Alphanumeric components are always held at their exact field width (space padded).
 */
public record TranRecord(
        String id,
        String typeCd,
        int catCd,
        String source,
        String desc,
        BigDecimal amt,
        long merchantId,
        String merchantName,
        String merchantCity,
        String merchantZip,
        String cardNum,
        String origTs,
        String procTs,
        String filler) implements FixedRecord {

    public static final int LENGTH = 350;

    public TranRecord {
        id = FixedWidth.fit(id, 16);
        typeCd = FixedWidth.fit(typeCd, 2);
        catCd = (int) CobolDecimal.toUnsignedField(catCd, 4);
        source = FixedWidth.fit(source, 10);
        desc = FixedWidth.fit(desc, 100);
        amt = CobolDecimal.toField(amt, 9, 2);
        merchantId = CobolDecimal.toUnsignedField(merchantId, 9);
        merchantName = FixedWidth.fit(merchantName, 50);
        merchantCity = FixedWidth.fit(merchantCity, 50);
        merchantZip = FixedWidth.fit(merchantZip, 10);
        cardNum = FixedWidth.fit(cardNum, 16);
        origTs = FixedWidth.fit(origTs, 26);
        procTs = FixedWidth.fit(procTs, 26);
        filler = FixedWidth.fit(filler, 20);
    }

    @Override
    public byte[] toBytes() {
        return new RecordWriter(LENGTH)
                .alpha(id, 16)
                .alpha(typeCd, 2)
                .unsigned(catCd, 4)
                .alpha(source, 10)
                .alpha(desc, 100)
                .signed(amt, 9, 2)
                .unsigned(merchantId, 9)
                .alpha(merchantName, 50)
                .alpha(merchantCity, 50)
                .alpha(merchantZip, 10)
                .alpha(cardNum, 16)
                .alpha(origTs, 26)
                .alpha(procTs, 26)
                .alpha(filler, 20)
                .toBytes();
    }

    public static TranRecord fromBytes(byte[] bytes) {
        RecordReader r = new RecordReader(bytes, LENGTH);
        return new TranRecord(
                r.alpha(16), r.alpha(2), r.unsignedInt(4), r.alpha(10), r.alpha(100),
                r.signed(9, 2), r.unsigned(9), r.alpha(50), r.alpha(50), r.alpha(10),
                r.alpha(16), r.alpha(26), r.alpha(26), r.alpha(20));
    }
}
