package com.carddemo.batch.core.record;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.carddemo.batch.core.BatchConfig;
import com.carddemo.batch.core.CardDemoFile;
import com.carddemo.batch.core.FixedWidth;
import com.carddemo.batch.core.io.SequentialFileReader;
import java.math.BigDecimal;
import java.util.List;
import java.util.function.Function;
import org.junit.jupiter.api.Test;

/** Every record in app/data/ASCII must survive fromBytes/toBytes byte for byte. */
class RecordRoundTripTest {

    private static List<byte[]> rawRecords(CardDemoFile file) {
        return SequentialFileReader.readAll(
                BatchConfig.defaultInputDir().resolve(file.fileName()), file.recordLength(), Function.identity());
    }

    private static void assertRoundTrip(CardDemoFile file, Function<byte[], ? extends com.carddemo.batch.core.FixedRecord> decoder,
            int expectedCount) {
        List<byte[]> records = rawRecords(file);
        assertEquals(expectedCount, records.size());
        for (byte[] raw : records) {
            assertArrayEquals(raw, decoder.apply(raw).toBytes(), FixedWidth.decode(raw));
        }
    }

    @Test
    void allDataFilesRoundTrip() {
        assertRoundTrip(CardDemoFile.DAILYTRAN, DailyTranRecord::fromBytes, 300);
        assertRoundTrip(CardDemoFile.DAILYTRAN, TranRecord::fromBytes, 300);
        assertRoundTrip(CardDemoFile.ACCTDATA, AccountRecord::fromBytes, 50);
        assertRoundTrip(CardDemoFile.CARDXREF, CardXrefRecord::fromBytes, 50);
        assertRoundTrip(CardDemoFile.TCATBAL, TranCatBalRecord::fromBytes, 50);
        assertRoundTrip(CardDemoFile.DISCGRP, DiscGroupRecord::fromBytes, 51);
        assertRoundTrip(CardDemoFile.TRANTYPE, TranTypeRecord::fromBytes, 7);
        assertRoundTrip(CardDemoFile.TRANCATG, TranCatRecord::fromBytes, 18);
    }

    @Test
    void dailyTranFieldOffsets() {
        DailyTranRecord t = DailyTranRecord.fromBytes(rawRecords(CardDemoFile.DAILYTRAN).get(0));
        assertEquals("0000000000683580", t.id());
        assertEquals("01", t.typeCd());
        assertEquals(1, t.catCd());
        assertEquals("POS TERM  ", t.source());
        assertEquals("Purchase at Abshire-Lowe", t.desc().strip());
        assertEquals(new BigDecimal("504.77"), t.amt());
        assertEquals(800000000L, t.merchantId());
        assertEquals("Abshire-Lowe", t.merchantName().strip());
        assertEquals("North Enoshaven", t.merchantCity().strip());
        assertEquals("72112     ", t.merchantZip());
        assertEquals("4859452612877065", t.cardNum());
        assertEquals("2022-06-10 19:27:53.000000", t.origTs());
        assertEquals(" ".repeat(26), t.procTs());
    }

    @Test
    void negativeDailyTranAmount() {
        DailyTranRecord t = DailyTranRecord.fromBytes(rawRecords(CardDemoFile.DAILYTRAN).get(1));
        assertEquals(new BigDecimal("-919.00"), t.amt());
    }

    @Test
    void accountFieldOffsets() {
        AccountRecord a = AccountRecord.fromBytes(rawRecords(CardDemoFile.ACCTDATA).get(0));
        assertEquals(1L, a.acctId());
        assertEquals("Y", a.activeStatus());
        assertEquals(new BigDecimal("194.00"), a.currBal());
        assertEquals(new BigDecimal("2020.00"), a.creditLimit());
        assertEquals(new BigDecimal("1020.00"), a.cashCreditLimit());
        assertEquals("2014-11-20", a.openDate());
        assertEquals("2025-05-20", a.expirationDate());
        assertEquals("2025-05-20", a.reissueDate());
        assertEquals(new BigDecimal("0.00"), a.currCycCredit());
        assertEquals(new BigDecimal("0.00"), a.currCycDebit());
        assertEquals("A000000000", a.addrZip());
        assertEquals(" ".repeat(10), a.groupId());
    }

    @Test
    void accountWithersPreserveOtherFields() {
        AccountRecord a = AccountRecord.fromBytes(rawRecords(CardDemoFile.ACCTDATA).get(0));
        AccountRecord b = a.withCurrBal(new BigDecimal("-1.5")).withCurrCycCredit(BigDecimal.ONE).withCurrCycDebit(BigDecimal.TEN);
        assertEquals("00000000001Y00000000015}00000020200{00000010200{2014-11-202025-05-202025-05-20"
                + "00000000010{00000000100{A000000000", FixedWidth.decode(b.toBytes()).substring(0, 112));
    }

    @Test
    void xrefKeysAndShortLinePadding() {
        CardXrefRecord x = CardXrefRecord.fromBytes(rawRecords(CardDemoFile.CARDXREF).get(0));
        assertEquals("0500024453765740", x.cardNum());
        assertEquals(50L, x.custId());
        assertEquals(50L, x.acctId());
        assertEquals(" ".repeat(14), x.filler());
    }

    @Test
    void tranCatBalKeyAndInitializedFiller() {
        TranCatBalRecord t = TranCatBalRecord.fromBytes(rawRecords(CardDemoFile.TCATBAL).get(0));
        assertEquals("00000000001010001", t.key());
        assertEquals(TranCatBalRecord.key(1, "01", 1), t.key());
        TranCatBalRecord created = new TranCatBalRecord(7, "02", 3, new BigDecimal("-12.34"));
        assertEquals("00000000007020003" + "0000000123M" + " ".repeat(22), FixedWidth.decode(created.toBytes()));
    }

    @Test
    void discGroupKeyAndRate() {
        DiscGroupRecord d = DiscGroupRecord.fromBytes(rawRecords(CardDemoFile.DISCGRP).get(0));
        assertEquals("A000000000010001", d.key());
        assertEquals(new BigDecimal("15.00"), d.intRate());
        assertEquals("DEFAULT   010001", DiscGroupRecord.key(DiscGroupRecord.DEFAULT_GROUP, "01", 1));
    }

    @Test
    void tranTypeAndCategory() {
        TranTypeRecord type = TranTypeRecord.fromBytes(rawRecords(CardDemoFile.TRANTYPE).get(0));
        assertEquals("01", type.tranType());
        assertEquals("Purchase", type.tranTypeDesc().strip());
        TranCatRecord cat = TranCatRecord.fromBytes(rawRecords(CardDemoFile.TRANCATG).get(0));
        assertEquals("010001", cat.key());
        assertEquals("Regular Sales Draft", cat.tranCatTypeDesc().strip());
    }

    @Test
    void rejectRecordLayout() {
        byte[] raw = rawRecords(CardDemoFile.DAILYTRAN).get(0);
        RejectRecord reject = new RejectRecord(DailyTranRecord.fromBytes(raw), 100, "INVALID CARD NUMBER FOUND");
        byte[] bytes = reject.toBytes();
        assertEquals(RejectRecord.LENGTH, bytes.length);
        assertEquals(FixedWidth.decode(raw), FixedWidth.decode(bytes).substring(0, 350));
        assertEquals("0100" + FixedWidth.fit("INVALID CARD NUMBER FOUND", 76), FixedWidth.decode(bytes).substring(350));
        assertEquals(reject, RejectRecord.fromBytes(bytes));
    }

    @Test
    void stringsAreNormalizedToFieldWidth() {
        TranTypeRecord t = new TranTypeRecord("01", "Purchase", null);
        assertEquals(new TranTypeRecord("01", FixedWidth.fit("Purchase", 50), "        "), t);
        assertThrows(IllegalArgumentException.class, () -> TranTypeRecord.fromBytes(new byte[59]));
    }
}
